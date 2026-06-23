/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.backend.common.lower.coroutines.getOrCreateFunctionWithContinuationStub
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrStarProjection
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.getAllSubstitutedSupertypes
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.isKFunction
import org.jetbrains.kotlin.ir.util.isKSuspendFunction
import org.jetbrains.kotlin.ir.util.isSuspendFunction
import org.jetbrains.kotlin.ir.util.overrides
import org.jetbrains.kotlin.ir.util.simpleFunctions
import org.jetbrains.kotlin.util.OperatorNameConventions
import kotlin.collections.plus

/**
 * This lowering is a hack to provide compatibility with leaked Kotlin/JVM implementation detail.
 *
 * On Kotin/JVM, `SuspendFunctionN<Args, Ret>` is implicitly implementing `FunctionN+1<Args, Continuation, Any(?)>`,
 * and vice versa, and it can be used for reusing continuation objects for performance improvements.
 *
 * Also, it is used in the `startCoroutineUninterceptedOrReturn` intrinsic.
 *
 * So we are adding the corresponding `Function{N+1}` into the list of supertypes.
 * At the current point, lowered suspend function signature is overriding its invoke method.
 */
open class AddFunctionSupertypeToSuspendFunctionLowering(open val context: CommonBackendContext) : DeclarationTransformer {
    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (declaration !is IrClass) return null
        return listOf(addMissingSupertypes(declaration))
    }

    // overridden for the K/Wasm stack switching compilation
    protected open fun IrSimpleFunction.getLowered() = if (isSuspend)
        getOrCreateFunctionWithContinuationStub(context)
    else
        this

    // overridden for the K/Wasm stack switching compilation
    protected open fun IrType.transformReturnType() =
        context.irBuiltIns.anyNType

    private fun IrClass.getInvokeFunction() = simpleFunctions().single {
        it.name == OperatorNameConventions.INVOKE
    }.getLowered()

    private fun addOverride(clazz: IrClass, alreadyOverridden: IrType, toOverride: IrType) {
        val alreadyOverriddenFunction = alreadyOverridden.classOrNull!!.owner.getInvokeFunction()
        val functionToOverride = toOverride.classOrNull!!.owner.getInvokeFunction()
        val invokeFunction = clazz.simpleFunctions().single { it.overrides(alreadyOverriddenFunction) }
        if (invokeFunction.modality == Modality.ABSTRACT) return
        clazz.superTypes += toOverride
        invokeFunction.overriddenSymbols += functionToOverride.symbol
    }

    private fun addMissingSupertypes(clazz: IrClass): IrClass {
        val suspendFunctionSuperTypes = getAllSubstitutedSupertypes(clazz).filter {
            // SuspendFunction class is some hack in old Kotlin/Native compiler versions.
            // It's not used now, but is considered as SuspendFunction-like class in isSuspendFunction util,
            // if found in old klib. We need just to ignore it.
            it.isSuspendFunction() && it.classOrNull?.owner?.name?.toString() != "SuspendFunction"
                    || it.isKSuspendFunction()
        }.toSet()

        val continuationClassSymbol = context.symbols.continuationClass

        fun IrSimpleType.getClassAt(index: Int) = (this.arguments.getOrNull(index) as? IrTypeProjection)?.type?.classOrNull

        val functionWithContinuationSuperTypes = getAllSubstitutedSupertypes(clazz).filter {
            (it.isFunction() || it.isKFunction()) &&
                    it.getClassAt(it.arguments.size - 2) == continuationClassSymbol
        }.toSet()

        for (suspendFunctionType in suspendFunctionSuperTypes) {
            val functionClassTypeArguments = suspendFunctionType.arguments.mapIndexed { index, argument ->
                val type = (argument as IrTypeProjection).type
                if (index == suspendFunctionType.arguments.indices.last) {
                    continuationClassSymbol.typeWith(type)
                } else {
                    type
                }
            } + (suspendFunctionType.arguments.last() as IrTypeProjection).type.transformReturnType()

            val genericFunctionSuperType =
                if (suspendFunctionType.isSuspendFunction()) {
                    context.irBuiltIns.functionN(functionClassTypeArguments.size - 1)
                } else {
                    context.irBuiltIns.kFunctionN(functionClassTypeArguments.size - 1)
                }

            val functionType = genericFunctionSuperType.typeWith(functionClassTypeArguments)

            addOverride(clazz, suspendFunctionType, functionType)
        }

        for (functionType in functionWithContinuationSuperTypes) {
            val suspendFunctionClassTypeArguments = functionType.arguments.dropLast(1).mapIndexed { index, argument ->
                val type = (argument as IrTypeProjection).type
                if (index == functionType.arguments.indices.last - 1) {
                    require(type.classOrNull == continuationClassSymbol)
                    when (val typeArgument = (type as IrSimpleType).arguments.single()) {
                        is IrTypeProjection -> typeArgument.type
                        is IrStarProjection -> context.irBuiltIns.anyNType
                    }
                } else {
                    type
                }
            }

            val genericSuspendFunctionType =
                if (functionType.isFunction()) {
                    context.irBuiltIns.suspendFunctionN(suspendFunctionClassTypeArguments.size - 1)
                } else {
                    context.irBuiltIns.kSuspendFunctionN(suspendFunctionClassTypeArguments.size - 1)
                }

            val suspendFunctionType = genericSuspendFunctionType.typeWith(suspendFunctionClassTypeArguments)
            addOverride(clazz, functionType, suspendFunctionType)
        }
        return clazz
    }
}

/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.transformers

import org.jetbrains.kotlin.backend.common.lower.irNot
import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.FilterVersion
import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.IrBuilderWithParent
import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.SequenceReplacement
import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.SequenceTransformer
import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.callRichFunctionReference
import org.jetbrains.kotlin.ir.builders.irEquals
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irIfThen
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturnTrue
import org.jetbrains.kotlin.ir.builders.irReturnableBlock
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration

internal class FilterReplacementCreator(val filter: SequenceTransformer.Filter) : TransformerReplacementCreator() {
    override fun addTransformerToBodyBuilder(
        sequenceReplacement: SequenceReplacement,
        shouldCheckShortCircuit: Boolean,
        builderWithParent: IrBuilderWithParent
    ): SequenceReplacement {
        val builder = builderWithParent.first
        val mainBodyBuilder = { sequenceVariable: IrValueDeclaration ->
            with(builder) {
                val block = irReturnableBlock(context.irBuiltIns.booleanType) {
                    val condition = when (filter.filterVersion) {
                        FilterVersion.Filter -> {
                            callRichFunctionReference(filter.function!!, builderWithParent.second, irGet(sequenceVariable))
                        }
                        FilterVersion.FilterNot -> {
                            irNot(callRichFunctionReference(filter.function!!, builderWithParent.second, irGet(sequenceVariable)))
                        }
                        FilterVersion.FilterNotNull -> {
                            irNot(irEquals((irGet(sequenceVariable)), irNull()))
                        }
                    }
                    +irIfThen(
                        context.irBuiltIns.unitType,
                        irNot(condition),
                        irReturnTrue().apply { this.returnTargetSymbol = returnableBlockSymbol })
                }
                addShortCircuitCheck(sequenceReplacement, shouldCheckShortCircuit, block, sequenceVariable, builder)
            }
        }
        val initialDeclarations = sequenceReplacement.initialDeclarations
        val finalExpression = sequenceReplacement.finalExpression
        return SequenceReplacement(initialDeclarations, mainBodyBuilder, finalExpression)
    }

    override val doesShortCircuit: Boolean = true
}

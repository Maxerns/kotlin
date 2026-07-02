/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.transformers

import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.IrBuilderWithParent
import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.SequenceReplacement
import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.SequenceTransformer
import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.callRichFunctionReference
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irEquals
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irIfThen
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturnTrue
import org.jetbrains.kotlin.ir.builders.irReturnableBlock
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration

internal class MapReplacementCreator(val map: SequenceTransformer.Map) :
    TransformerReplacementCreator() {
    override fun addTransformerToBodyBuilder(
        sequenceReplacement: SequenceReplacement,
        shouldCheckShortCircuit: Boolean,
        builderWithParent: IrBuilderWithParent
    ): SequenceReplacement {
        val builder = builderWithParent.first
        val mapIndexedVariable = builder.scope.createTemporaryVariable(
            builder.irInt(0),
            isMutable = true,
            nameHint = "mapIndexedVariable"
        )
        val mainBodyBuilder = { sequenceVariable: IrValueDeclaration ->
            with(builder) {
                val mappedFunctionCall = if (map.isIndexed)
                    callRichFunctionReference(
                        map.function,
                        builderWithParent.second,
                        irGet(mapIndexedVariable),
                        irGet(sequenceVariable)
                    )
                else
                    callRichFunctionReference(map.function, builderWithParent.second, irGet(sequenceVariable))

                val mapResultVariable = scope.createTemporaryVariable(mappedFunctionCall, nameHint = "mapResult")
                val block = irReturnableBlock(context.irBuiltIns.booleanType) {
                    +mapResultVariable
                    if (map.isIndexed) {
                        +irSet(mapIndexedVariable, irCall(context.irBuiltIns.intPlusSymbol).apply {
                            dispatchReceiver = irGet(mapIndexedVariable)
                            arguments[1] = irInt(1)
                        })
                    }
                    if (map.isNotNull) {
                        val filterResult =
                            scope.createTemporaryVariable(irEquals(irGet(mapResultVariable), irNull()), nameHint = "filterResult")
                        +filterResult
                        // if (mapResult == null) return true, which is equivalent to continue
                        +irIfThen(
                            context.irBuiltIns.unitType,
                            irGet(filterResult),
                            irReturnTrue().apply { this.returnTargetSymbol = returnableBlockSymbol }
                        )
                    }
                }
                addShortCircuitCheck(sequenceReplacement, shouldCheckShortCircuit, block, mapResultVariable, builder)
            }
        }
        val initialDeclarations =
            if (map.isIndexed) sequenceReplacement.initialDeclarations + mapIndexedVariable
            else sequenceReplacement.initialDeclarations
        val finalExpression = sequenceReplacement.finalExpression
        return SequenceReplacement(initialDeclarations, mainBodyBuilder, finalExpression)
    }

    override val doesShortCircuit: Boolean = false
}

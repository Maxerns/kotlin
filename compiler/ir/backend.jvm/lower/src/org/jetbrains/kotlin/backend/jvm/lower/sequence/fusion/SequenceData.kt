/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion

import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.producers.GenerateSequenceStrategy
import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.producers.ProducerStrategy
import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.producers.SequenceConstructorStrategy
import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.producers.SequenceOfStrategy
import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.producers.UnknownVariableStrategy
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrRichFunctionReference
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.types.IrType

internal sealed class SequenceTransformer {
    class Map(val function: IrRichFunctionReference, val isIndexed: Boolean, val isNotNull: Boolean) : SequenceTransformer()
    class Filter(val function: IrRichFunctionReference?, val filterVersion: FilterVersion) : SequenceTransformer()
    class Take(val argument: IrExpression) : SequenceTransformer()
}

internal class SequenceData(
    val sequenceSource: SequenceSource,
    val transformers: List<SequenceTransformer>
)

// sequenceSource is what the sequence was created from, to be substituted if the loop is to be fused
internal sealed class SequenceSource {
    class SequenceOf(val elements: List<IrExpression>, val type: IrType) : SequenceSource()
    class Variable(val variable: IrValueSymbol) : SequenceSource()
    class AsSequence(val iterable: IrExpression) : SequenceSource()
    class Sequence(val sequenceScope: IrRichFunctionReference) : SequenceSource()
    class GenerateSequence(
        val initialValue: GenerateSequenceInitialValue,
        val generatingFunction: IrRichFunctionReference,
        val sequenceElementType: IrType
    ) : SequenceSource()

    internal fun createProducerStrategy(
        builder: IrBuilderWithScope,
        context: JvmBackendContext,
    ): ProducerStrategy = when (this) {
        is AsSequence -> UnknownVariableStrategy(this.iterable)
        is GenerateSequence -> GenerateSequenceStrategy(this)
        is SequenceOf -> SequenceOfStrategy(this)
        is Variable -> UnknownVariableStrategy(builder.irGet(this.variable.owner))
        is Sequence -> SequenceConstructorStrategy(
            this.sequenceScope,
            context,
        )
    }
}

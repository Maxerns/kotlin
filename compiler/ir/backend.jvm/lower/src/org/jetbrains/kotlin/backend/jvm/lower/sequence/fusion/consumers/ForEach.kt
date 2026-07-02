/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.consumers

import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.ConsumerBodyBuilder
import org.jetbrains.kotlin.backend.jvm.lower.sequence.fusion.callRichFunctionReference
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturnTrue
import org.jetbrains.kotlin.ir.builders.irReturnableBlock
import org.jetbrains.kotlin.ir.builders.irUnit
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrRichFunctionReference

internal class ForEachConsumerStrategy(data: ConsumerData, expression: IrCall) : ConsumerStrategy(data, expression) {
    override val returnsElement: Boolean = false
    override fun initializeState(): List<IrVariable> = emptyList()

    override fun getConsumerBuilder(): ConsumerBodyBuilder? {
        val expression = expression as IrCall
        val function = expression.arguments.getOrNull(1) as? IrRichFunctionReference ?: return null
        return { sequenceElement ->
            data.builder.irReturnableBlock(data.context.irBuiltIns.booleanType) {
                +callRichFunctionReference(function, data.parent, irGet(sequenceElement))
                +irReturnTrue().apply { returnTargetSymbol = returnableBlockSymbol }
            }
        }
    }

    override fun finalizeResult(): IrExpression = data.builder.irUnit()
}

/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.validation.checkers.context

import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrScript
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.validation.IrValidationError
import org.jetbrains.kotlin.ir.validation.ScopeStack
import org.jetbrains.kotlin.ir.validation.checkers.IrChecker
import java.util.Collections.emptySet

class CheckerContext(
    val irBuiltIns: IrBuiltIns,
    val file: IrFile,
    private val reportError: (IrValidationError) -> Unit,
) {
    private var _parentChain: List<IrElement> = emptyList()
    val parentChain: List<IrElement>
        get() = _parentChain

    private var _typeParameterScopeStack = ScopeStack<IrTypeParameterSymbol>()
    val typeParameterScopeStack: ScopeStack<IrTypeParameterSymbol>
        get() = _typeParameterScopeStack

    private var _valueSymbolScopeStack = ScopeStack<IrValueSymbol>()
    val valueSymbolScopeStack: ScopeStack<IrValueSymbol>
        get() = _valueSymbolScopeStack

    private var _offsetRanges: List<OffsetRange> = emptyList()
    val offsetRanges: List<OffsetRange>
        get() = _offsetRanges

    var withinAnnotationUsageSubTree: Boolean = false
        private set

    fun error(element: IrElement, cause: IrValidationError.Cause, message: String) =
        reportError(IrValidationError(file, element, cause, message, parentChain))

    context(checker: IrChecker)
    fun error(element: IrElement, message: String) = error(element, checker, message)

    fun withTypeParametersInScope(container: IrTypeParametersContainer): CheckerContext {
        val newScope = typeParameterScopeStack.withNewScope(
            outerScopesAreInvisible = container is IrClass && !container.isInner && container.visibility != DescriptorVisibilities.LOCAL,
            populateScope = { container.typeParameters.forEach { add(it.symbol) } },
        )
        return copy(
            typeParameterScopeStack = newScope
        )
    }

    fun withScopeOwner(owner: IrElement, populateScope: MutableSet<IrValueSymbol>.() -> Unit = { }): CheckerContext {
        val newScope = valueSymbolScopeStack.withNewScope(
            isGlobalScope = owner is IrScript,
            outerScopesAreInvisible = owner is IrClass && !owner.isInner && owner.visibility != DescriptorVisibilities.LOCAL,
            populateScope = populateScope
        )
        return copy(
            valueSymbolScopeStack = newScope
        )
    }

    fun withinAnnotationUsageSubTree(): CheckerContext {
        if (withinAnnotationUsageSubTree) return this
        return copy(withinAnnotationUsageSubTree = true)
    }

    fun copy(
        parentChain: List<IrElement> = this.parentChain,
        typeParameterScopeStack: ScopeStack<IrTypeParameterSymbol> = this.typeParameterScopeStack,
        valueSymbolScopeStack: ScopeStack<IrValueSymbol> = this.valueSymbolScopeStack,
        offsetRanges: List<OffsetRange> = this.offsetRanges,
        withinAnnotationUsageSubTree: Boolean = this.withinAnnotationUsageSubTree
    ): CheckerContext {
        return CheckerContext(irBuiltIns, file, reportError).apply {
            _parentChain = parentChain
            _typeParameterScopeStack = typeParameterScopeStack
            _valueSymbolScopeStack = valueSymbolScopeStack
            _offsetRanges = offsetRanges
            this.withinAnnotationUsageSubTree = withinAnnotationUsageSubTree
        }
    }
}

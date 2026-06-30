/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.validation

class ScopeStack<E> private constructor(private val scopes: List<Scope<E>>) {
    private class Scope<E>(val isGlobal: Boolean) {
        val values = mutableSetOf<E>()
    }

    constructor() : this(emptyList())

    /**
     * Creates a new scope.
     *
     * @param isGlobalScope Whether values of this new scope are always visible in nested scopes,
     *   even for those nested scopes where [outerScopesAreInvisible] is `true`.
     * @param outerScopesAreInvisible Whether values of outer scopes are invisible in this new scope, except when an outer scope is global.
     */
    fun withNewScope(
        isGlobalScope: Boolean = false,
        outerScopesAreInvisible: Boolean = false,
        populateScope: MutableSet<E>.() -> Unit = { },
    ): ScopeStack<E> {
        return if (outerScopesAreInvisible) {
            val globals = scopes.filterTo(mutableListOf(), Scope<E>::isGlobal)
            ScopeStack(globals + Scope<E>(isGlobal = false).apply { values.populateScope() })
        } else {
            ScopeStack(scopes.toList() + Scope<E>(isGlobalScope).apply { values.populateScope() })
        }
    }

    /**
     * This method explicitly mutates the inner state. Visited value must be kept until we change to a new scope
     */
    fun addToCurrentScope(element: E) {
        scopes.lastOrNull()?.values?.add(element)
    }

    fun isVisibleInCurrentScope(element: E): Boolean = scopes.any { it.values.contains(element) }
}

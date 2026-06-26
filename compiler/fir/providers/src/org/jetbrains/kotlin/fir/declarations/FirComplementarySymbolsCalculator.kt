/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSessionComponent
import org.jetbrains.kotlin.fir.SessionHolder
import org.jetbrains.kotlin.fir.declarations.utils.isClass
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.isJavaNonAbstractSealed
import org.jetbrains.kotlin.fir.resolve.getSuperTypes
import org.jetbrains.kotlin.fir.resolve.isSubclassOf
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol

interface FirComplementarySymbolsCalculator : FirSessionComponent {
    context(holder: SessionHolder)
    fun getComplementarySymbolsFor(symbol: FirRegularClassSymbol): Set<FirClassSymbol<*>>
}

object FirDefaultComplementarySymbolsCalculator : FirComplementarySymbolsCalculator {
    context(holder: SessionHolder)
    fun FirClassSymbol<*>.isSubclassOf(other: FirClassSymbol<*>): Boolean =
        isSubclassOf(other.toLookupTag(), holder.session, isStrict = false, lookupInterfaces = true)

    context(holder: SessionHolder)
    fun areUnrelated(a: FirClassSymbol<*>, b: FirClassSymbol<*>): Boolean =
        !a.isSubclassOf(b) && !b.isSubclassOf(a)

    context(holder: SessionHolder)
    fun FirRegularClassSymbol.getImmediateSuperTypes(): Set<FirRegularClassSymbol> =
        getSuperTypes(holder.session, recursive = false)
            .mapNotNullTo(mutableSetOf()) { it.toRegularClassSymbol() }

    data class RelevantSealedUniverse(
        val leaves: MutableSet<FirClassSymbol<*>> = mutableSetOf(),
        val visited: MutableSet<FirRegularClassSymbol> = mutableSetOf(),
    )

    private fun List<RelevantSealedUniverse>.merge() = RelevantSealedUniverse(
        leaves = flatMapTo(mutableSetOf()) { it.leaves },
        visited = flatMapTo(mutableSetOf()) { it.visited },
    )

    private val relevantSealedUniverseCache = mutableMapOf<FirRegularClassSymbol, RelevantSealedUniverse>()

    context(holder: SessionHolder)
    fun FirRegularClassSymbol.collectRelevantSealedUniverse(): RelevantSealedUniverse =
        relevantSealedUniverseCache.getOrPut(this) {
            val superTypes = getImmediateSuperTypes()
            val mergedUniverse = superTypes.map { it.collectRelevantSealedUniverse() }.merge()

            for (superType in superTypes) {
                superType.collectAllSubclassesTo(mergedUniverse.leaves, holder.session, mergedUniverse.visited)
            }

            mergedUniverse
        }

    private val unrelatedSubclassesCache = mutableMapOf<FirRegularClassSymbol, Set<FirClassSymbol<*>>>()

    context(holder: SessionHolder)
    fun FirRegularClassSymbol.collectUnrelatedSubclasses(): Set<FirClassSymbol<*>> =
        unrelatedSubclassesCache.getOrPut(this) {
            val mergedUnrelated = getImmediateSuperTypes().flatMapTo(mutableSetOf()) { it.collectUnrelatedSubclasses() }
            val universe = collectRelevantSealedUniverse()

            universe.leaves.filterTo(mutableSetOf()) {
                it in mergedUnrelated || (isFinal || it.isFinal || isClass && it.isClass) && areUnrelated(this, it)
            }
        }

    context(holder: SessionHolder)
    override fun getComplementarySymbolsFor(symbol: FirRegularClassSymbol): Set<FirClassSymbol<*>> =
        symbol.collectUnrelatedSubclasses()
}

val FirSession.complementarySymbolsCalculator: FirComplementarySymbolsCalculator by FirSession.sessionComponentAccessor()

fun FirClassSymbol<*>.collectAllSubclasses(session: FirSession): Set<FirClassSymbol<*>> {
    return mutableSetOf<FirClassSymbol<*>>().apply { collectAllSubclassesTo(this, session) }
}

private fun FirClassSymbol<*>.collectAllSubclassesTo(
    destination: MutableSet<FirClassSymbol<*>>,
    session: FirSession,
    visited: MutableSet<FirRegularClassSymbol> = mutableSetOf(),
) {
    if (this !is FirRegularClassSymbol) {
        destination.add(this)
        return
    }
    if (!visited.add(this)) return
    when {
        fir.modality == Modality.SEALED -> {
            if (fir.isJavaNonAbstractSealed == true) {
                destination.add(this)
            }

            fir.getSealedClassInheritors(session).forEach {
                val symbol = session.symbolProvider.getClassLikeSymbolByClassId(it) as? FirRegularClassSymbol
                symbol?.collectAllSubclassesTo(destination, session, visited)
            }
        }
        else -> destination.add(this)
    }
}

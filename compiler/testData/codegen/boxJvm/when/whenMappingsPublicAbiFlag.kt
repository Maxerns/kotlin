// TARGET_BACKEND: JVM
// WITH_STDLIB

// Tests that the isPublicAbi flag (bit 7 of extraInt) is correctly set on WhenMappings classes.
// The existing whenMappingsMetadataVisibility.kt only checks visibility bits 8-10; this test
// covers the orthogonal isPublicAbi dimension.
//
// WhenMappings used from a non-private inline function must have isPublicAbi=true because the
// inliner will not regenerate them at call sites — callers read the mapping from the library class.
// WhenMappings used only from regular (non-inline) functions must have isPublicAbi=false.

// FILE: util.kt

package util

enum class E { A, B }

val globalE: E = E.A

inline fun fooPublicInline(): Int = when (globalE) {
    E.A -> 1
    E.B -> 2
}

// FILE: test.kt

package test

import util.*

enum class F { X, Y }

val globalF: F = F.X

fun fooRegular(): Int = when (globalF) {
    F.X -> 1
    F.Y -> 2
}

private const val PUBLIC_ABI_FLAG = 1 shl 7

private fun isPublicAbi(javaClass: Class<*>): Boolean =
    javaClass.getAnnotation(Metadata::class.java).extraInt and PUBLIC_ABI_FLAG != 0

fun box(): String {
    fooPublicInline()
    fooRegular()

    // E is used in a public inline function → its WhenMappings class must be public ABI
    val utilWhenMappings = Class.forName("util.UtilKt").declaredClasses.single { it.simpleName == "WhenMappings" }
    if (!isPublicAbi(utilWhenMappings)) {
        return "Fail: WhenMappings used from public inline should be public ABI (isPublicAbi flag missing)"
    }

    // F is used only in a regular (non-inline) function → its WhenMappings must NOT be public ABI
    val testWhenMappings = Class.forName("test.TestKt").declaredClasses.single { it.simpleName == "WhenMappings" }
    if (isPublicAbi(testWhenMappings)) {
        return "Fail: WhenMappings used only from a regular function should NOT be public ABI"
    }

    return "OK"
}

// TARGET_BACKEND: JVM
// WITH_STDLIB

// FILE: util.kt

package util

annotation class Ann(val value: String)

inline fun createAnn(): Ann = Ann("OK")

// FILE: test.kt

package test

import util.*

private const val SYNTHETIC_CLASS_VISIBILITY_SHIFT = 8
private const val SYNTHETIC_CLASS_VISIBILITY_MASK = 0b111
private const val LOCAL_VISIBILITY = 5
private const val PUBLIC_ABI_FLAG = 1 shl 7

private fun metadataExtraInt(javaClass: Class<*>): Int =
    javaClass.getAnnotation(Metadata::class.java).extraInt

private fun syntheticClassVisibility(javaClass: Class<*>): Int =
    (metadataExtraInt(javaClass) shr SYNTHETIC_CLASS_VISIBILITY_SHIFT) and SYNTHETIC_CLASS_VISIBILITY_MASK

private fun isPublicAbi(javaClass: Class<*>): Boolean =
    metadataExtraInt(javaClass) and PUBLIC_ABI_FLAG != 0

fun box(): String {
    val ann = createAnn()

    val visibility = syntheticClassVisibility(ann.javaClass)
    if (visibility != LOCAL_VISIBILITY) {
        return "Fail: expected LOCAL visibility (5) for annotation implementation from inline function, got $visibility"
    }
    if (!isPublicAbi(ann.javaClass)) {
        return "Fail: expected annotation implementation class from public inline function to be public ABI"
    }

    return ann.value
}

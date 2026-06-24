// TARGET_BACKEND: JVM
// WITH_STDLIB
// SAM_CONVERSIONS: CLASS

// FILE: util.kt

package util

// internal inline functions are non-private and contribute to module ABI:
// synthetic classes they create should be treated identically to public inline functions.
internal inline fun samWrapperInternalInline(noinline job: () -> Unit): Class<*> {
    val samWrapper = Runnable(job)
    return samWrapper::class.java
}

// FILE: test.kt

package test

import util.*

private const val SYNTHETIC_CLASS_VISIBILITY_SHIFT = 8
private const val SYNTHETIC_CLASS_VISIBILITY_MASK = 0b111
private const val PUBLIC_ABI_FLAG = 1 shl 7
private const val PUBLIC_VISIBILITY = 3

private fun metadataExtraInt(javaClass: Class<*>): Int =
    javaClass.getAnnotation(Metadata::class.java).extraInt

private fun syntheticClassVisibility(javaClass: Class<*>): Int =
    (metadataExtraInt(javaClass) shr SYNTHETIC_CLASS_VISIBILITY_SHIFT) and SYNTHETIC_CLASS_VISIBILITY_MASK

private fun isPublicAbi(javaClass: Class<*>): Boolean =
    metadataExtraInt(javaClass) and PUBLIC_ABI_FLAG != 0

fun box(): String {
    val samWrapperClass = samWrapperInternalInline {}

    val visibility = syntheticClassVisibility(samWrapperClass)
    if (visibility != PUBLIC_VISIBILITY) { // NOTE: in future potentially can be lowered to internal instead of public
        return "Fail: expected PUBLIC visibility (3) for SAM wrapper in internal inline function, got $visibility"
    }
    if (!isPublicAbi(samWrapperClass)) {
        return "Fail: expected SAM wrapper in internal inline function to be public ABI"
    }

    return "OK"
}

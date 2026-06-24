// TARGET_BACKEND: JVM
// WITH_STDLIB

// FILE: util.kt

package util

interface A {
    fun result(): String
}

fun objectClass(): Class<*> {
    val anonymousObject = object : A {
        override fun result(): String = "OK"
    }
    return anonymousObject::class.java
}

inline fun objectClassInline(): Class<*> {
    val anonymousObject = object : A {
        override fun result(): String = "OK"
    }
    return anonymousObject::class.java
}

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
    val nonEscapedClass = objectClass()
    val escapedClass = objectClassInline()

    if (isPublicAbi(nonEscapedClass)) {
        return "Fail: expected non-escaped anonymous object to not be public ABI"
    }
    if (!isPublicAbi(escapedClass)) {
        return "Fail: expected anonymous object from public inline function to be public ABI"
    }

    val nonEscapedVisibility = syntheticClassVisibility(nonEscapedClass)
    if (nonEscapedVisibility != LOCAL_VISIBILITY) {
        return "Fail: expected LOCAL visibility (5) for non-escaped anonymous object, got $nonEscapedVisibility"
    }
    val escapedVisibility = syntheticClassVisibility(escapedClass)
    if (escapedVisibility != LOCAL_VISIBILITY) {
        return "Fail: expected LOCAL visibility (5) for anonymous object from inline function, got $escapedVisibility"
    }

    return "OK"
}

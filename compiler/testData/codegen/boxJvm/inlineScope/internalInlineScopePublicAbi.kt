// TARGET_BACKEND: JVM
// WITH_STDLIB
// SAM_CONVERSIONS: CLASS

// Tests that classes created inside an `internal inline` function are marked isPublicAbi=true.
// The existing tests (lambdaMetadataVisibility, callableReferenceMetadataVisibility, etc.) only
// exercise `public inline` functions. Per the implementation, isInPublicInlineScope is true for
// any non-private inline scope, which includes `internal`. This test covers that dimension.

// FILE: util.kt

package util

import kotlin.jvm.JvmSerializableLambda

fun callableRefTarget(): String = "OK"

internal inline fun lambdaClassFromInternalInline(): Class<*> {
    val lambda = @JvmSerializableLambda { "OK" }
    return lambda::class.java
}

internal inline fun callableRefClassFromInternalInline(): Class<*> {
    val ref = ::callableRefTarget
    return ref::class.java
}

interface I {
    fun result(): String
}

internal inline fun anonymousObjectClassFromInternalInline(): Class<*> {
    val obj = object : I {
        override fun result() = "OK"
    }
    return obj::class.java
}

internal inline fun samWrapperClassFromInternalInline(noinline job: () -> Unit): Class<*> {
    val wrapper = Runnable(job)
    return wrapper::class.java
}

// FILE: test.kt

package test

import util.*

private const val PUBLIC_ABI_FLAG = 1 shl 7

private fun isPublicAbi(javaClass: Class<*>): Boolean =
    javaClass.getAnnotation(Metadata::class.java).extraInt and PUBLIC_ABI_FLAG != 0

fun box(): String {
    if (!isPublicAbi(lambdaClassFromInternalInline())) {
        return "Fail: lambda class in internal inline should be public ABI"
    }

    if (!isPublicAbi(callableRefClassFromInternalInline())) {
        return "Fail: callable reference class in internal inline should be public ABI"
    }

    if (!isPublicAbi(anonymousObjectClassFromInternalInline())) {
        return "Fail: anonymous object class in internal inline should be public ABI"
    }

    if (!isPublicAbi(samWrapperClassFromInternalInline {})) {
        return "Fail: SAM wrapper class in internal inline should be public ABI"
    }

    return "OK"
}

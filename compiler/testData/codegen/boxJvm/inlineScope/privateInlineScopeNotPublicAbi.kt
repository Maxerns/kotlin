// TARGET_BACKEND: JVM
// WITH_STDLIB
// SAM_CONVERSIONS: CLASS

// Tests that classes created inside a `private inline` function do NOT have isPublicAbi=true.
// Private inline functions are the one inline scope where isInPublicInlineScope returns false,
// because private functions are never visible outside the declaring file and the inliner will
// regenerate their contained local classes at each call site rather than reusing the originals.
// The classes observed at runtime (the regenerated call-site copies) should have isPublicAbi=false.

import kotlin.jvm.JvmSerializableLambda

fun callableRefTarget(): String = "OK"

private inline fun lambdaClassFromPrivateInline(): Class<*> {
    val lambda = @JvmSerializableLambda { "OK" }
    return lambda::class.java
}

private inline fun callableRefClassFromPrivateInline(): Class<*> {
    val ref = ::callableRefTarget
    return ref::class.java
}

private interface I {
    fun result(): String
}

private inline fun anonymousObjectClassFromPrivateInline(): Class<*> {
    val obj = object : I {
        override fun result() = "OK"
    }
    return obj::class.java
}

private inline fun samWrapperClassFromPrivateInline(noinline job: () -> Unit): Class<*> {
    val wrapper = Runnable(job)
    return wrapper::class.java
}

private const val PUBLIC_ABI_FLAG = 1 shl 7

private fun isPublicAbi(javaClass: Class<*>): Boolean =
    javaClass.getAnnotation(Metadata::class.java).extraInt and PUBLIC_ABI_FLAG != 0

fun box(): String {
    if (isPublicAbi(lambdaClassFromPrivateInline())) {
        return "Fail: lambda class in private inline should NOT be public ABI"
    }

    if (isPublicAbi(callableRefClassFromPrivateInline())) {
        return "Fail: callable reference class in private inline should NOT be public ABI"
    }

    if (isPublicAbi(anonymousObjectClassFromPrivateInline())) {
        return "Fail: anonymous object class in private inline should NOT be public ABI"
    }

    if (isPublicAbi(samWrapperClassFromPrivateInline {})) {
        return "Fail: SAM wrapper class in private inline should NOT be public ABI"
    }

    return "OK"
}

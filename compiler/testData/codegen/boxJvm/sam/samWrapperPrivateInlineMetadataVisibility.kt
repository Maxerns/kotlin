// TARGET_BACKEND: JVM
// WITH_STDLIB
// SAM_CONVERSIONS: CLASS

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

private inline fun samWrapperPrivateInline(noinline job: () -> Unit): Class<*> {
    val samWrapper = Runnable(job)
    return samWrapper::class.java
}

fun box(): String {
    val samWrapperClass = samWrapperPrivateInline {}

    val visibility = syntheticClassVisibility(samWrapperClass)
    if (visibility != LOCAL_VISIBILITY) {
        return "Fail: expected LOCAL visibility (5) for SAM wrapper in private inline function, got $visibility"
    }
    if (isPublicAbi(samWrapperClass)) {
        return "Fail: expected SAM wrapper in private inline function to not be public ABI"
    }

    return "OK"
}

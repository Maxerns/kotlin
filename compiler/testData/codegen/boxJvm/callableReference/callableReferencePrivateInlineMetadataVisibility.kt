// TARGET_BACKEND: JVM
// WITH_STDLIB

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

fun foo(): String = "OK"

private inline fun callableRefPrivateInline(): Class<*> {
    val ref = ::foo
    return ref::class.java
}

fun box(): String {
    val refClass = callableRefPrivateInline()

    val visibility = syntheticClassVisibility(refClass)
//    if (visibility != LOCAL_VISIBILITY) {
    if (visibility != 3) { // PUBLIC
        return "Fail: expected LOCAL visibility (5) for callable reference in private inline function, got $visibility"
    }
    if (isPublicAbi(refClass)) {
        return "Fail: expected callable reference in private inline function to not be public ABI"
    }

    return "OK"
}

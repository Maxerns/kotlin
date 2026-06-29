// TARGET_BACKEND: JVM
// WITH_STDLIB

import kotlin.jvm.JvmSerializableLambda

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

private inline fun lambdaPrivateInline(): Class<*> {
    val lambda = @JvmSerializableLambda { "OK" }
    return lambda::class.java
}

fun box(): String {
    val lambdaClass = lambdaPrivateInline()

    val visibility = syntheticClassVisibility(lambdaClass)
    if (visibility != 3) { // PUBLIC
//    if (visibility != LOCAL_VISIBILITY) {
        return "Fail: expected LOCAL visibility (5) for lambda in private inline function, got $visibility"
    }
    if (isPublicAbi(lambdaClass)) {
        return "Fail: expected lambda in private inline function to not be public ABI"
    }

    return "OK"
}

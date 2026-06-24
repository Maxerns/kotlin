// TARGET_BACKEND: JVM
// WITH_STDLIB

// The annotation implementation transformer always forces LOCAL visibility on the implementation
// class, regardless of the annotation class's own visibility. This guards against a regression
// where someone tries to propagate the annotation's visibility to its implementation class.

private const val SYNTHETIC_CLASS_VISIBILITY_SHIFT = 8
private const val SYNTHETIC_CLASS_VISIBILITY_MASK = 0b111
private const val INTERNAL_VISIBILITY = 0
private const val LOCAL_VISIBILITY = 5

private fun syntheticClassVisibility(javaClass: Class<*>): Int {
    val extraInt = javaClass.getAnnotation(Metadata::class.java).extraInt
    return (extraInt shr SYNTHETIC_CLASS_VISIBILITY_SHIFT) and SYNTHETIC_CLASS_VISIBILITY_MASK
}

internal annotation class InternalAnn(val value: String)

fun box(): String {
    val ann = InternalAnn("OK")

    val visibility = syntheticClassVisibility(ann.javaClass)
    if (visibility == INTERNAL_VISIBILITY) {
        return "Fail: annotation implementation class should not inherit annotation's internal visibility; expected LOCAL (5), got INTERNAL (0)"
    }
    if (visibility != LOCAL_VISIBILITY) {
        return "Fail: expected LOCAL visibility (5) for internal annotation implementation class, got $visibility"
    }

    return ann.value
}

// TARGET_BACKEND: JVM
// WITH_STDLIB

// FILE: test.kt

package test

// All enum WhenMappings for a given file-class share a single WhenMappings inner class.
// isPublicAbi on that class is true if ANY mapping inside it comes from a public inline scope
// (MappedEnumWhenLowering uses `any { it.isPublicAbi }` to compute the class-level flag).
// Consequence: even E2's mapping data ends up in a public-ABI class because E1's does.

enum class E1 { A, B }
enum class E2 { X, Y }

val e1: E1 = E1.A
val e2: E2 = E2.X

// E1 is used from a public inline function → its mapping is public ABI → whole WhenMappings class is public ABI
inline fun publicWhenE1(): Int = when (e1) {
    E1.A -> 1
    E1.B -> 2
}

// E2 is used only from non-inline code → its own mapping is not public ABI,
// but it resides in the same WhenMappings class as E1's mapping
fun nonInlineWhenE2(): Int = when (e2) {
    E2.X -> 10
    E2.Y -> 20
}

private const val PUBLIC_ABI_FLAG = 1 shl 7

private fun isPublicAbi(javaClass: Class<*>): Boolean =
    javaClass.getAnnotation(Metadata::class.java).extraInt and PUBLIC_ABI_FLAG != 0

fun box(): String {
    publicWhenE1()
    nonInlineWhenE2()

    val whenMappings = Class.forName("test.TestKt").declaredClasses.single { it.simpleName == "WhenMappings" }

    if (!isPublicAbi(whenMappings)) {
        return "Fail: WhenMappings class should be public ABI because it contains a mapping for E1 " +
                "which is used from a public inline function"
    }

    return "OK"
}

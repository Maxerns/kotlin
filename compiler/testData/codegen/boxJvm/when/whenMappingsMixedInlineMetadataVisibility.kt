// TARGET_BACKEND: JVM
// WITH_STDLIB

// FILE: test.kt

package test

// Both functions share the same WhenMappings class for enum E (same file-level class).
// isPublicAbi on that class is OR'd across all usages: even though privateWhen alone
// would not set it, publicWhen does — and the flag must not be cleared by the private usage.

enum class E { A, B }

val e: E = E.A

private inline fun privateWhen(): Int = when (e) {
    E.A -> 1
    E.B -> 2
}

inline fun publicWhen(): Int = when (e) {
    E.A -> 1
    E.B -> 2
}

private const val PUBLIC_ABI_FLAG = 1 shl 7

private fun isPublicAbi(javaClass: Class<*>): Boolean =
    javaClass.getAnnotation(Metadata::class.java).extraInt and PUBLIC_ABI_FLAG != 0

fun box(): String {
    privateWhen()
    publicWhen()

    val whenMappings = Class.forName("test.TestKt").declaredClasses.single { it.simpleName == "WhenMappings" }

    if (!isPublicAbi(whenMappings)) {
        return "Fail: WhenMappings used in a public inline function should be public ABI " +
                "even when the same mapping is also used in a private inline function"
    }

    return "OK"
}

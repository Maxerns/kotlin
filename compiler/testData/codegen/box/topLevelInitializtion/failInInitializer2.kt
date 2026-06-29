// IGNORE_BACKEND: JS_IR, JS_IR_ES6, WASM_JS, WASM_WASI
// FULL_JDK

// FILE: lib.kt
val x: String = computeX()

fun computeX(): String = throw IllegalStateException("1")

val y: String = computeY()

fun computeY(): String = "2"

// FILE: main.kt
fun box() : String {
    try {
        x
        return "FAIL 1.1"
    } catch(t: ExceptionInInitializerError) {
        val cause = t.cause
        if (cause !is IllegalStateException) return "FAIL 1.2: cause must be IllegalStateException, was ${cause?.let { it::class }}"
        if (cause.message != "1") return "FAIL 1.3: message must be '1', was '${cause.message}'"
        if (t.message != null) return "FAIL 1.4: message must be null, got ${t.message}"
    }
    try {
        y
        return "FAIL 2"
    } catch(t: Error) {
        // On JVM < 20, t.cause is null, but on JVM >= 20, it's ExceptionInInitializerError.
    }
    return "OK"
}

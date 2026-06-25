// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// IGNORE_BACKEND: NATIVE
// WITH_STDLIB

fun box(): String {
    try {
        val v1: Any = Array<Int>(10) {it}
        val v2 = v1 as Array<String>
        v2.forEach {
            println(it)
        }
        return "FAIL: expected exception"
    } catch (e: Throwable) {
        return "OK"
    }
}

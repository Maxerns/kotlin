// LANGUAGE: +CompanionBlocksAndExtensions
// IGNORE_IR_DESERIALIZATION_TEST: JS_IR, NATIVE

// MODULE: lib
// FILE: lib.kt
var initLog = ""

open class Parent {
    companion {
        val parentValue = run {
            initLog += "P"
            "parent"
        }
    }
}

// MODULE: main(lib)
// FILE: main.kt
class Child : Parent() {
    companion {
        val childValue = run {
            initLog += "C"
            "child"
        }
    }
}

fun box(): String {
    if (Child.childValue != "child") return "FAIL child"
    if (initLog != "PC") return "FAIL order: $initLog"
    if (Parent.parentValue != "parent") return "FAIL parent"

    // Expected: "PC"
    // Actual on K/Wasm single-module: "CP"
    if (initLog != "PC") return "FAIL order: $initLog"

    return "OK"
}

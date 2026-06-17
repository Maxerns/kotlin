// LANGUAGE: +CompanionBlocksAndExtensions
// IGNORE_BACKEND: NATIVE
// ^^^ Native currently initializes the child before the parent (`CP` instead of `PC`).
// WASM_IGNORE_FOR: mode=single-module
// ^^^ KT-87329
// WASM_IGNORE_FOR: mode=multi-module
// ^^^ KT-87109

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
    if (Parent.parentValue != "parent") return "FAIL parent"
    if (initLog != "PC") return "FAIL order: $initLog"

    return "OK"
}

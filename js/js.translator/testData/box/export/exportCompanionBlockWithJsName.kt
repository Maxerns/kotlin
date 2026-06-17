// RUN_PLAIN_BOX_FUNCTION
// LANGUAGE: +CompanionBlocksAndExtensions

// MODULE: lib
// FILE: lib.kt
@JsExport
class Test {
    companion {
        @JsName("renamedFun")
        fun originalFun(value: String = "X"): String = value

        @JsName("renamedVal")
        val originalVal: String = "OK"

        @JsName("renamedMutable")
        var originalMutable: String = "INITIAL"
    }
}

// FILE: main.js
function box() {
    var Test = this.lib.Test;

    if (typeof Test.originalFun !== "undefined") return "FAIL: originalFun should be renamed away"
    if (typeof Test.originalVal !== "undefined") return "FAIL: originalVal should be renamed away"
    if (typeof Test.originalMutable !== "undefined") return "FAIL: originalMutable should be renamed away"

    if (Test.renamedFun() !== "X") return "FAIL renamedFun default value"
    if (Test.renamedFun("Y") !== "Y") return "FAIL renamedFun argument"
    if (Test.renamedVal !== "OK") return "FAIL renamedVal"
    if (Test.renamedMutable !== "INITIAL") return "FAIL renamedMutable before mutation"

    Test.renamedMutable = "CHANGED"
    if (Test.renamedMutable !== "CHANGED") return "FAIL renamedMutable after mutation"

    return "OK"
}

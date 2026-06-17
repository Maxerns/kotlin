// RUN_PLAIN_BOX_FUNCTION
// LANGUAGE: +CompanionBlocksAndExtensions

// MODULE: lib
// FILE: lib.kt
@JsExport
class Test {
    companion {
        fun bar(value: String = "BARRRR"): String = value

        val foo = "FOOOO"
        var mutable = "INITIAL"
    }
}

// FILE: main.js
function box() {
    var Test = this.lib.Test;

    if (Test.bar() !== "BARRRR") return "Problem with companion block static method default value"
    if (Test.bar("CHANGED") !== "CHANGED") return "Problem with companion block static method argument"
    if (Test.foo !== "FOOOO") return "Problem with companion block static property"
    if (Test.mutable !== "INITIAL") return "Problem with mutable companion block property before mutation"
    Test.mutable = "CHANGED"
    if (Test.mutable !== "CHANGED") return "Problem with mutable companion block property after mutation"

    return "OK"
}

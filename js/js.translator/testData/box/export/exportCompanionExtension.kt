// RUN_PLAIN_BOX_FUNCTION
// LANGUAGE: +CompanionBlocksAndExtensions

// MODULE: lib
// FILE: lib.kt
@JsExport
class Test

@JsExport
companion fun Test.extFun(value: String = "EXT"): String = value

@JsExport
@JsName("renamedExtFun")
companion fun Test.originalExtFun(value: String = "RENAMED"): String = value

// FILE: main.js
function box() {
    var lib = this.lib;

    if (lib.extFun() !== "EXT") return "FAIL companion extension default value"
    if (lib.extFun("CHANGED") !== "CHANGED") return "FAIL companion extension argument"
    if (typeof lib.originalExtFun !== "undefined") return "FAIL original companion extension name leaked"
    if (lib.renamedExtFun() !== "RENAMED") return "FAIL renamed companion extension default value"
    if (lib.renamedExtFun("CHANGED") !== "CHANGED") return "FAIL renamed companion extension argument"

    return "OK"
}

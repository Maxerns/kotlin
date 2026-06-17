// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// LANGUAGE: +CompanionBlocksAndExtensions
// MODULE: JS_TESTS
// FILE: companion-blocks.kt

package foo

@JsExport
class ExportedWithCompanionBlock {
    companion {
        fun append(value: String = "K"): String {
            mutable = value
            return readOnly + mutable
        }

        val readOnly: String = "O"
        var mutable: String = ""
    }
}

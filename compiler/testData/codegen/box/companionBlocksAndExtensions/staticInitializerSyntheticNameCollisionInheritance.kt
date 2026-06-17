// LANGUAGE: +CompanionBlocksAndExtensions
// IGNORE_BACKEND: JVM, NATIVE

var initLog = ""

open class Parent {
    companion {
        val parentTrigger = run { initLog += "P"; "parent" }

        fun static_init(): String = "parentFun"
        val static_init_called: String = "parentProp"
    }
}

class Child : Parent() {
    companion {
        val childTrigger = run { initLog += "C"; "child" }

        fun static_init(): String = "childFun"
        val static_init_called: String = "childProp"
    }
}

fun box(): String {
    if (Child.childTrigger != "child") return "FAIL child trigger"
    if (Parent.parentTrigger != "parent") return "FAIL parent trigger"
    if (initLog != "PC") return "FAIL order: $initLog"

    if (Child.static_init() != "childFun") return "FAIL child fun"
    if (Parent.static_init() != "parentFun") return "FAIL parent fun"
    if (Child.static_init_called != "childProp") return "FAIL child prop"
    if (Parent.static_init_called != "parentProp") return "FAIL parent prop"

    return "OK"
}

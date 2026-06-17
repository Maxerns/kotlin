// LANGUAGE: +CompanionBlocksAndExtensions

var initLog = ""

private fun init(tag: String, value: String): String {
    initLog += tag
    return value
}

enum class TriggeredByProperty(val value: String) {
    Entry(init("1", "entry"));

    companion {
        val blockValue = init("2", "block")
    }
}

enum class TriggeredByFunction(val value: String) {
    Entry(init("A", "entry"));

    companion {
        val blockValue = init("B", "block")
        fun touch() = "touch"
    }
}

fun box(): String {
    if (TriggeredByProperty.blockValue != "block") return "FAIL property"
    if (TriggeredByProperty.Entry.value != "entry") return "FAIL entry property"
    if (initLog != "12") return "FAIL property order: $initLog"

    initLog = ""
    if (TriggeredByFunction.touch() != "touch") return "FAIL function"
    if (TriggeredByFunction.Entry.value != "entry") return "FAIL entry function"
    if (TriggeredByFunction.blockValue != "block") return "FAIL block function"
    if (initLog != "AB") return "FAIL function order: $initLog"

    return "OK"
}

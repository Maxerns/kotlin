// LANGUAGE: +CompanionBlocksAndExtensions
// IGNORE_BACKEND: JVM, NATIVE

var initLog = ""

class CrossA {
    companion {
        val trigger = run { initLog += "A"; "triggerA" }

        var static_init: String = "userVarA"

        val static_init_called: String = "userPropA"
    }
}

class CrossB {
    companion {
        val trigger = run { initLog += "B"; "triggerB" }

        fun static_init_called(): String = "userFunB"

        fun static_init(): String = "userFunB2"
    }
}

fun box(): String {
    if (CrossA.trigger != "triggerA") return "FAIL CrossA trigger"
    if (CrossA.static_init != "userVarA") return "FAIL CrossA.static_init read"
    CrossA.static_init = "mutatedA"
    if (CrossA.static_init != "mutatedA") return "FAIL CrossA.static_init after mutation"
    if (CrossA.static_init_called != "userPropA") return "FAIL CrossA.static_init_called"

    if (CrossB.trigger != "triggerB") return "FAIL CrossB trigger"
    if (CrossB.static_init_called() != "userFunB") return "FAIL CrossB.static_init_called()"
    if (CrossB.static_init() != "userFunB2") return "FAIL CrossB.static_init()"

    if (initLog != "AB") return "FAIL order: $initLog"

    return "OK"
}

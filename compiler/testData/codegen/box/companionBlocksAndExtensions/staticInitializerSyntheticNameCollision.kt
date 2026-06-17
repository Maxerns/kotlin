// LANGUAGE: +CompanionBlocksAndExtensions

var initLog = ""

class Collision {
    companion {
        val trigger = run {
            initLog += "T"
            "trigger"
        }

        fun static_init(): String = "userFun"
        val static_init_called: String = "userProperty"
    }
}

fun box(): String {
    val result = Collision.trigger + "|" + Collision.static_init() + "|" + Collision.static_init_called + "|" + initLog
    return if (result == "trigger|userFun|userProperty|T") "OK" else "FAIL: $result"
}

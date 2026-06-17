// LANGUAGE: +CompanionBlocksAndExtensions
// IGNORE_BACKEND: JVM_IR, NATIVE
// ^^^ JVM_IR is intentionally ignored: KT-85853 tracks interface companion block
// properties emitted as illegal interface fields.
// Native currently initializes the class before transitive superinterfaces
// (`C I0 I1` instead of `I0 I1 C`).

var initLog = ""

interface I0 {
    companion {
        val i0Value = run {
            initLog += "I0 "
            "i0"
        }
    }

    fun inheritedDefault(): String = "inherited"
}

interface I1 : I0 {
    companion {
        val i1Value = run {
            initLog += "I1 "
            "i1"
        }
    }

    fun bridgeDefault(): String = inheritedDefault()
}

class C : I1 {
    companion {
        val cValue = run {
            initLog += "C "
            "c"
        }
    }
}

fun box(): String {
    if (C.cValue != "c") return "FAIL c"
    if (I0.i0Value != "i0") return "FAIL i0"
    if (I1.i1Value != "i1") return "FAIL i1"
    if (initLog != "I0 I1 C ") return "FAIL order: $initLog"

    return "OK"
}

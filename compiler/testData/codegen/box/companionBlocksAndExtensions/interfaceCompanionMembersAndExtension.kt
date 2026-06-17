// LANGUAGE: +CompanionBlocksAndExtensions
// IGNORE_BACKEND: JVM, JVM_IR
// ^^^KT-85853 tracks interface companion block

interface MyInterface {
    companion {
        fun interfaceFun() = "O"
        val interfaceVal = "K"
    }
}

companion fun MyInterface.extFun() = "OK"

class Impl : MyInterface {
    fun useInterfaceCompanion() = MyInterface.interfaceFun() + MyInterface.interfaceVal
}

fun box(): String {
    if (MyInterface.interfaceFun() != "O") return "FAIL 1"
    if (MyInterface.interfaceVal != "K") return "FAIL 2"
    if (MyInterface.extFun() != "OK") return "FAIL 3"

    val impl = Impl()
    return impl.useInterfaceCompanion()
}

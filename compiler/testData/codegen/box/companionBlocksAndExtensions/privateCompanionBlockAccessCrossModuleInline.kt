// LANGUAGE: +CompanionBlocksAndExtensions
// IGNORE_HEADER_MODE: JS_IR

// MODULE: lib
// FILE: lib.kt
class Secret {
    companion {
        private var token = "O"
        private fun readToken(): String = token
    }
}

@Suppress("INVISIBLE_REFERENCE")
internal inline fun leakSecret(): String {
    Secret.token = Secret.readToken() + "K"
    return Secret.token
}

// MODULE: main()(lib)
// FILE: main.kt
fun box(): String {
    return leakSecret()
}

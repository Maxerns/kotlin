// WITH_STDLIB
// LANGUAGE: +CompanionBlocksAndExtensions

class GenericHost<T> {
    companion {
        fun foo(value: String): String = "foo:$value"
        val marker: String = "marker"
    }
}

typealias StringHost = GenericHost<String>

fun box(): String {
    val directFun: (String) -> String = GenericHost::foo
    val aliasFun: (String) -> String = StringHost::foo
    val aliasProp: () -> String = StringHost::marker

    val directResult = directFun("O")
    if (directResult != "foo:O") return "FAIL direct function reference: $directResult"

    val aliasFunResult = aliasFun("K")
    if (aliasFunResult != "foo:K") return "FAIL typealias function reference: $aliasFunResult"

    val aliasPropResult = aliasProp()
    if (aliasPropResult != "marker") return "FAIL typealias property reference: $aliasPropResult"

    return "OK"
}

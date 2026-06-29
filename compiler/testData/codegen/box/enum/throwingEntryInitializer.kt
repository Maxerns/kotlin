// FULL_JDK

enum class Color(val s: String) {
    BLACK("black"),
    HATSUNE_MIKU(run { throw IllegalStateException("miku is not a color") });
}

enum class ThrowsError(val s: String) {
    NONTHROWING("throwing"),
    THROWING(run { throw Error("huh") });
}

fun box(): String {
    try {
        Color.BLACK
        return "FAIL 1.1: should throw"
    } catch (e: ExceptionInInitializerError) {
        val cause = e.cause
        if (cause !is IllegalStateException) return "FAIL 1.2: cause must be IllegalStateException, was ${cause?.let { it::class }}"
        if (cause.message != "miku is not a color") return "FAIL 1.3: message must be 'miku is not a color', was '${cause.message}'"
        if (e.message != null) return "FAIL 1.4: message must be null, got ${e.message}"
    }

    try {
        Color.BLACK
        return "FAIL 2.1: should throw"
    } catch (e: Error) {
        if (e.cause != null) return "FAIL 2.2: cause must be null, got ${e.cause}"
//        if (e.message != "Could not initialize class Color") return "FAIL 2.3: unexpected message: '${e.message}'"
    }

    try {
        ThrowsError.NONTHROWING
        return "FAIL 3.1: should throw"
    } catch (e: Error) {
        if (e.cause != null) return "FAIL 3.2: cause must be null, got ${e.cause}"
        if (e.message != "huh") return "FAIL 3.3: message must be 'huh', was '${e.message}'"
    }

    try {
        ThrowsError.NONTHROWING
        return "FAIL 4.1: should throw"
    } catch (e: Error) {
        if (e.cause != null) return "FAIL 4.2: cause must be null, got ${e.cause}"
//        if (e.message != "Could not initialize class ThrowsError") return "FAIL 4.3: unexpected message: '${e.message}'"
    }

    return "OK"
}

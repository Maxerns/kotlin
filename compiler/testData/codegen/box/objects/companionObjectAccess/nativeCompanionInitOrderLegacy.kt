// See companionInitOrderWithSuperclass for the common treatment.
// TARGET_BACKEND: NATIVE
// LANGUAGE: -CompanionBlocksAndExtensions

var l = ""
private fun log(t: String) {
    l += t + "\n"
}

// Each test uses its own class hierarchy so companions are initialized fresh.

// companion-only access (no instance created)
open class B1 {
    init { log("B1.init#1") }
    companion object { init { log("B1.Companion") } }
    init { log("B1.init#2") }
}
class A1 : B1() {
    init { log("A1.init#1") }
    companion object { init { log("A1.Companion") } }
    init { log("A1.init#2") }
}

// instance creation (triggers both companions then instance inits)
open class B2 {
    init { log("B2.init#1") }
    companion object { init { log("B2.Companion") } }
    init { log("B2.init#2") }
}
class A2 : B2() {
    init { log("A2.init#1") }
    companion object { init { log("A2.Companion") } }
    init { log("A2.init#2") }
}

// companion access then instance creation
open class B3 {
    init { log("B3.init#1") }
    companion object { init { log("B3.Companion") } }
    init { log("B3.init#2") }
}
class A3 : B3() {
    init { log("A3.init#1") }
    companion object { init { log("A3.Companion") } }
    init { log("A3.init#2") }
}

// instance creation then companion access
open class B4 {
    init { log("B4.init#1") }
    companion object { init { log("B4.Companion") } }
    init { log("B4.init#2") }
}
class A4 : B4() {
    init { log("A4.init#1") }
    companion object { init { log("A4.Companion") } }
    init { log("A4.init#2") }
}

// 3-level hierarchy with companion access only
open class C5 {
    companion object { init { log("C5.Companion") } }
}
open class B5 : C5() {
    companion object { init { log("B5.Companion") } }
}
class A5 : B5() {
    companion object { init { log("A5.Companion") } }
}

// 3-level hierarchy with instance creation
open class C6 {
    init { log("C6.init") }
    companion object { init { log("C6.Companion") } }
}
open class B6 : C6() {
    init { log("B6.init") }
    companion object { init { log("B6.Companion") } }
}
class A6 : B6() {
    init { log("A6.init") }
    companion object { init { log("A6.Companion") } }
}

// intermediate class with no companion, companion access only otherwise.
open class C7 {
    companion object { init { log("C7.Companion") } }
}
open class B7 : C7()  // no companion
class A7 : B7() {
    companion object { init { log("A7.Companion") } }
}

// intermediate class with no companion; instance creation
open class C8 {
    init { log("C8.init") }
    companion object { init { log("C8.Companion") } }
}
open class B8 : C8()  // no companion
class A8 : B8() {
    init { log("A8.init") }
    companion object { init { log("A8.Companion") } }
}

// multiple interface inheritance
interface I9 {
    fun i() {}
    companion object { init { log("I9.Companion") } }
}
interface J9 {
    fun j() {}
    companion object { init { log("J9.Companion") } }
}
interface K9 : I9 {
    fun k() {}
    companion object { init { log("K9.Companion") } }
}
interface L9 : J9 {
    fun l() {}
    companion object { init { log("L9.Companion") } }
}
interface M9 {
    companion object { init { log("M9.Companion") } }
}
open class B9 : J9, K9 {
    companion object { init { log("B9.Companion") } }
}
class A9: B9(), L9, M9 {
    companion object { init { log("A9.Companion") } }
}

// multiple interface inheritance; with instance creation
interface I10 {
    fun i() {}
    companion object { init { log("I10.Companion") } }
}
interface J10 {
    fun j() {}
    companion object { init { log("J10.Companion") } }
}
interface K10 : I10 {
    fun k() {}
    companion object { init { log("K10.Companion") } }
}
interface L10 : J10 {
    fun l() {}
    companion object { init { log("L10.Companion") } }
}
interface M10 {
    companion object { init { log("M10.Companion") } }
}
open class B10 : J10, K10 {
    init { log("B10.init") }
    companion object { init { log("B10.Companion") } }
}
class A10: B10(), L10, M10 {
    init { log("A10.init") }
    companion object { init { log("A10.Companion") } }
}

fun box(): String {
    l = ""
    A1
    val r1 = l
    if (r1 != "A1.Companion\n") return "fail test1: '$r1'"

    l = ""
    A2()
    val r2 = l
    if (r2 != "A2.Companion\nB2.Companion\nB2.init#1\nB2.init#2\nA2.init#1\nA2.init#2\n") return "fail test2: '$r2'"

    l = ""
    A3
    log("--")
    A3()
    val r3 = l
    if (r3 != "A3.Companion\n--\nB3.Companion\nB3.init#1\nB3.init#2\nA3.init#1\nA3.init#2\n") return "fail test3: '$r3'"

    l = ""
    A4()
    log("--")
    A4
    val r4 = l
    if (r4 != "A4.Companion\nB4.Companion\nB4.init#1\nB4.init#2\nA4.init#1\nA4.init#2\n--\n") return "fail test4: '$r4'"

    l = ""
    A5
    val r5 = l
    if (r5 != "A5.Companion\n") return "fail test5: '$r5'"

    l = ""
    A6()
    val r6 = l
    if (r6 != "A6.Companion\nB6.Companion\nC6.Companion\nC6.init\nB6.init\nA6.init\n") return "fail test6: '$r6'"

    l = ""
    A7
    val r7 = l
    if (r7 != "A7.Companion\n") return "fail test7: '$r7'"

    l = ""
    A8()
    val r8 = l
    if (r8 != "A8.Companion\nC8.Companion\nC8.init\nA8.init\n") return "fail test8: '$r8'"

    l = ""
    A9
    val r9 = l
    if (r9 != "A9.Companion\n") return "fail test9: '$r9'"

    l = ""
    A10()
    val r10 = l
    if (r10 != "A10.Companion\nB10.Companion\nB10.init\nA10.init\n") return "fail test10: '$r10'"

    return "OK"
}

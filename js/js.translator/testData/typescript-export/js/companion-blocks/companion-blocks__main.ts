import ExportedWithCompanionBlock = JS_TESTS.foo.ExportedWithCompanionBlock;

function assert(condition: boolean) {
    if (!condition) {
        throw "Assertion failed";
    }
}

function box(): string {
    assert(ExportedWithCompanionBlock.readOnly === "O");
    assert(ExportedWithCompanionBlock.mutable === "");
    assert(ExportedWithCompanionBlock.append() === "OK");
    assert(ExportedWithCompanionBlock.mutable === "K");
    ExportedWithCompanionBlock.mutable = "Q";
    assert(ExportedWithCompanionBlock.mutable === "Q");
    assert(ExportedWithCompanionBlock.append("L") === "OL");
    assert(ExportedWithCompanionBlock.mutable === "L");

    return "OK";
}

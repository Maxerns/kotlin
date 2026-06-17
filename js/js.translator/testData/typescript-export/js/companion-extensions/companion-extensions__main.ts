import append = JS_TESTS.foo.append;

function assert(condition: boolean) {
    if (!condition) {
        throw "Assertion failed";
    }
}

function box(): string {
    assert(append() === "OK");
    assert(append("L") === "OL");

    return "OK";
}

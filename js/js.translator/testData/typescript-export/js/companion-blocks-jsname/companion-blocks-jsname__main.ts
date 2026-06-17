import WithJsName = JS_TESTS.foo.WithJsName;

function assert(condition: boolean) {
    if (!condition) {
        throw "Assertion failed";
    }
}

function box(): string {
    assert(WithJsName.renamedVal === "K");
    assert(WithJsName.renamedFun() === "OK");

    return "OK";
}

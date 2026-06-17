import WithInternal = JS_TESTS.foo.WithInternal;

function assert(condition: boolean) {
    if (!condition) {
        throw "Assertion failed";
    }
}

function box(): string {
    assert(WithInternal.publicVal === "y");

    if (false) {
        // @ts-expect-error internal companion block property must not be exported
        WithInternal.secret;
    }

    return "OK";
}

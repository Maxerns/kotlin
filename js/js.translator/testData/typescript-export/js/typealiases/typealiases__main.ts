// import foo = JS_TESTS.foo;
//
// function assert(condition: boolean) {
//     if (!condition) {
//         throw "Assertion failed";
//     }
// }
//
// // Verify that typealiases used in exported functions work correctly at runtime
// assert(foo.consumeMyInt(41) === 42);
//
// foo.consumeCallback(() => { });
//
// assert(foo.consumeGenericAlias(new foo.GenericClass("hello")) === "hello");
//
// const obj = new foo.ClassUsingAlias(1, "test");
// assert(obj.id === 1);
// assert(obj.name === "test");
//
// function box(): string {
//     return "OK";
// }

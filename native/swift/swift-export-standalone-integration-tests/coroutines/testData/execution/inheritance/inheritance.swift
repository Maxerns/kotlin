import Main
import Testing

// A Swift subclass overriding a Kotlin `suspend` method. Dispatching to the override happens through
// the reverse async bridge: Kotlin's virtual call hits the patched vtable trampoline, which suspends
// the Kotlin coroutine and awaits the Swift override via a Task.
@Test
func swiftCanOverrideKotlinSuspendMethod() async throws {
    class SwiftDerived: AsyncBase {
        override func greet(name: String) async throws -> String {
            return "Swift: \(name)"
        }
    }

    let derived = SwiftDerived()

    // Direct Swift dispatch.
    #expect(try await derived.greet(name: "A") == "Swift: A")
    // Call through Kotlin: the reverse async bridge must dispatch to the Swift override.
    #expect(try await callGreet(base: derived, name: "B") == "Swift: B")

    // The original Kotlin class is untouched.
    let base = AsyncBase()
    #expect(try await callGreet(base: base, name: "C") == "Kotlin: C")
}

// The non-virtual ("_direct") forward async bridge: a Swift subclass that overrides only `greet`
// must still be able to inherit `count` without infinitely recursing through the patched vtable slot.
@Test
func swiftSubclassInheritsNonOverriddenSuspendMethod() async throws {
    class PartialDerived: AsyncBase {
        override func greet(name: String) async throws -> String { "Swift: \(name)" }
        // count() intentionally NOT overridden.
    }

    let derived = PartialDerived()
    #expect(try await callGreet(base: derived, name: "X") == "Swift: X")
    // Inherited Kotlin `count` reached via the direct-dispatch bridge (no recursion).
    #expect(try await callCount(base: derived) == 42)
}

// The non-virtual ("_direct") forward async bridge also backs `super` calls from a Swift override.
@Test
func swiftSuspendOverrideCanCallSuper() async throws {
    class WrappingDerived: AsyncBase {
        override func greet(name: String) async throws -> String {
            let inner = try await super.greet(name: name)
            return "wrapped(\(inner))"
        }
    }

    let derived = WrappingDerived()
    #expect(try await derived.greet(name: "Y") == "wrapped(Kotlin: Y)")
    #expect(try await callGreet(base: derived, name: "Z") == "wrapped(Kotlin: Z)")
}

// Swift overriding a Kotlin `suspend` *interface* method, reached via Kotlin-side interface dispatch.
@Test
func swiftCanOverrideKotlinSuspendInterfaceMethod() async throws {
    class SwiftSpeaker: AsyncSpeakerBase {
        override func speak() async throws -> String { "Swift speaks" }
    }

    let speaker = SwiftSpeaker()
    #expect(try await callSpeak(s: speaker) == "Swift speaks")

    let base = AsyncSpeakerBase()
    #expect(try await callSpeak(s: base) == "Kotlin speaks")
}

// A Swift class that inherits a Kotlin class and first-adopts a Kotlin `suspend`-interface, inheriting
// its DEFAULT method (does not override `describe`). The inherited default must dispatch non-virtually
// (via the `_direct` async bridge) so it never recurses through the patched itable; its open self-call
// to `tag()` must reach the Swift override.
@Test
func swiftInheritsKotlinSuspendInterfaceDefault() async throws {
    class MyAsyncDefaulter: AsyncSpeakerBase, AsyncDefaulter {
        func tag() async throws -> String { "swift-tag" }
        // describe() intentionally NOT overridden -> inherits the Kotlin async default.
    }
    let d = MyAsyncDefaulter()

    // Direct Swift dispatch: inherited async default runs; its open self-call reaches the Swift override.
    #expect(try await d.describe() == "default-describe(swift-tag)")
    // Kotlin-side dispatch must terminate (no infinite recursion) and yield the same result.
    #expect(try await callAsyncDescribe(d: d) == "default-describe(swift-tag)")
}

// A Swift override that throws: the error must travel back through the reverse bridge's exception
// channel into the Kotlin coroutine and then out to the caller.
@Test
func swiftSuspendOverrideCanThrow() async throws {
    class SwiftThrower: AsyncThrower {
        struct Failure: Error {}
        override func boom() async throws -> String {
            throw Failure()
        }
    }

    let thrower = SwiftThrower()
    await #expect(throws: (any Error).self) {
        _ = try await callBoom(t: thrower)
    }
}

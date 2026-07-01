declare namespace JS_TESTS {
    type Nullable<T> = T | null | undefined
    function KtSingleton<T>(): T & (abstract new() => any);
    namespace foo {
        function consumeMyInt(value: foo.MyInt): foo.MyInt;
        function consumeCallback(cb: Nullable<foo.SimpleCallback>): Nullable<void>;
        function consumeGenericAlias(value: foo.AliasGenericClass<string>): string;
        class ClassUsingAlias {
            constructor(id: foo.MyInt, name: foo.MyString);
            get id(): foo.MyInt;
            get name(): foo.MyString;
        }
        namespace ClassUsingAlias {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new () => ClassUsingAlias;
            }
        }
    }
    namespace foo {
        class SomeClass {
            constructor(value: string);
            get value(): string;
        }
        namespace SomeClass {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new () => SomeClass;
            }
        }
        class GenericClass<T> {
            constructor(value: T);
            get value(): T;
        }
        namespace GenericClass {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new <T>() => GenericClass<T>;
            }
        }
        class TwoGenericParamsClass<A, B> {
            constructor(first: A, second: B);
            get first(): A;
            get second(): B;
        }
        namespace TwoGenericParamsClass {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new <A, B>() => TwoGenericParamsClass<A, B>;
            }
        }
        interface SomeInterface {
            readonly prop: string;
            readonly __doNotUseOrImplementIt: {
                readonly "foo.SomeInterface": unique symbol;
            };
        }
        interface SomeExternalInterface {
        }
        abstract class SomeEnum {
            private constructor();
            static get A(): foo.SomeEnum & {
                get name(): "A";
                get ordinal(): 0;
            };
            static get B(): foo.SomeEnum & {
                get name(): "B";
                get ordinal(): 1;
            };
            static get C(): foo.SomeEnum & {
                get name(): "C";
                get ordinal(): 2;
            };
            static values(): [typeof foo.SomeEnum.A, typeof foo.SomeEnum.B, typeof foo.SomeEnum.C];
            static valueOf(value: string): foo.SomeEnum;
            get name(): "A" | "B" | "C";
            get ordinal(): 0 | 1 | 2;
        }
        namespace SomeEnum {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new () => SomeEnum;
            }
        }
        abstract class SomeObject extends KtSingleton<SomeObject.$metadata$.constructor>() {
            private constructor();
        }
        namespace SomeObject {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                abstract class constructor {
                    get value(): string;
                    private constructor();
                }
            }
        }
        class ClassWithConstraint<T extends foo.SomeClass> {
            constructor(value: T);
            get value(): T;
        }
        namespace ClassWithConstraint {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new <T extends foo.SomeClass>() => ClassWithConstraint<T>;
            }
        }
        class ClassWithNestedTypealiases {
            constructor();
        }
        namespace ClassWithNestedTypealiases {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new () => ClassWithNestedTypealiases;
            }
            type NestedInt = number;
            type NestedString = string;
            type NestedClassAlias = foo.SomeClass;
            type NestedGenericAlias<T> = foo.GenericClass<T>;
            type NestedConcreteGenericAlias = foo.GenericClass<string>;
            type NestedCallback = () => void;
            type NestedNullable = Nullable<number>;
        }
        class GenericClassWithNestedTypealiases<T> {
            constructor();
        }
        namespace GenericClassWithNestedTypealiases {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new <T>() => GenericClassWithNestedTypealiases<T>;
            }
            type NestedAlias = number;
            type NestedGenericAlias<T, R> = foo.TwoGenericParamsClass<T, R>;
        }
        interface InterfaceWithNestedTypealiases {
            readonly __doNotUseOrImplementIt: {
                readonly "foo.InterfaceWithNestedTypealiases": unique symbol;
            };
        }
        namespace InterfaceWithNestedTypealiases {
            type InterfaceNestedInt = number;
            type InterfaceNestedClassAlias = foo.SomeClass;
            type InterfaceNestedGenericAlias<T> = foo.GenericClass<T>;
            type InterfaceNestedCallback = (p0: number) => string;
        }
        abstract class ObjectWithNestedTypealiases extends KtSingleton<ObjectWithNestedTypealiases.$metadata$.constructor>() {
            private constructor();
        }
        namespace ObjectWithNestedTypealiases {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                abstract class constructor {
                    private constructor();
                }
                namespace constructor {
                    type ObjectNestedInt = number;
                    type ObjectNestedClassAlias = foo.SomeClass;
                    type ObjectNestedGenericAlias<T> = foo.GenericClass<T>;
                }
            }
        }
        class ClassWithCompanionTypealiases {
            constructor();
        }
        namespace ClassWithCompanionTypealiases {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new () => ClassWithCompanionTypealiases;
            }
            abstract class Companion extends KtSingleton<Companion.$metadata$.constructor>() {
                private constructor();
            }
            namespace Companion {
                /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
                namespace $metadata$ {
                    abstract class constructor {
                        private constructor();
                    }
                    namespace constructor {
                        type CompanionNestedInt = number;
                        type CompanionNestedString = string;
                        type CompanionNestedGenericAlias<T> = foo.GenericClass<T>;
                    }
                }
            }
        }
        class ClassWithNamedCompanionTypealiases {
            constructor();
        }
        namespace ClassWithNamedCompanionTypealiases {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new () => ClassWithNamedCompanionTypealiases;
            }
            abstract class Named extends KtSingleton<Named.$metadata$.constructor>() {
                private constructor();
            }
            namespace Named {
                /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
                namespace $metadata$ {
                    abstract class constructor {
                        private constructor();
                    }
                    namespace constructor {
                        type NamedCompanionNestedAlias = string;
                        type NamedCompanionGenericAlias<T> = foo.GenericClass<T>;
                    }
                }
            }
        }
        abstract class EnumWithNestedTypealiases {
            private constructor();
            static get X(): foo.EnumWithNestedTypealiases & {
                get name(): "X";
                get ordinal(): 0;
            };
            static get Y(): foo.EnumWithNestedTypealiases & {
                get name(): "Y";
                get ordinal(): 1;
            };
            static get Z(): foo.EnumWithNestedTypealiases & {
                get name(): "Z";
                get ordinal(): 2;
            };
            static values(): [typeof foo.EnumWithNestedTypealiases.X, typeof foo.EnumWithNestedTypealiases.Y, typeof foo.EnumWithNestedTypealiases.Z];
            static valueOf(value: string): foo.EnumWithNestedTypealiases;
            get name(): "X" | "Y" | "Z";
            get ordinal(): 0 | 1 | 2;
        }
        namespace EnumWithNestedTypealiases {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new () => EnumWithNestedTypealiases;
            }
            type EnumNestedInt = number;
            type EnumNestedClassAlias = foo.SomeClass;
        }
        class OpenClassWithNestedTypealiases {
            constructor();
        }
        namespace OpenClassWithNestedTypealiases {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new () => OpenClassWithNestedTypealiases;
            }
            type OpenClassNestedAlias = string;
            type OpenClassNestedGenericAlias<T> = foo.GenericClass<T>;
        }
        abstract class AbstractClassWithNestedTypealiases {
            constructor();
        }
        namespace AbstractClassWithNestedTypealiases {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new () => AbstractClassWithNestedTypealiases;
            }
            type AbstractNestedAlias = number;
            type AbstractNestedClassAlias = foo.SomeClass;
        }
        class OuterWithNested {
            constructor();
        }
        namespace OuterWithNested {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new () => OuterWithNested;
            }
            type OuterNestedAlias = string;
            class InnerNested {
                constructor();
            }
            namespace InnerNested {
                /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
                namespace $metadata$ {
                    const constructor: abstract new () => InnerNested;
                }
                type DeeplyNestedAlias = number;
                type DeeplyNestedClassAlias = foo.SomeClass;
            }
        }
        abstract class SealedClassWithNestedTypealiases {
            private constructor();
        }
        namespace SealedClassWithNestedTypealiases {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new () => SealedClassWithNestedTypealiases;
            }
            type SealedNestedAlias = number;
            class Sub extends foo.SealedClassWithNestedTypealiases.$metadata$.constructor {
                constructor();
            }
            namespace Sub {
                /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
                namespace $metadata$ {
                    const constructor: abstract new () => Sub;
                }
            }
        }
        interface SealedInterfaceWithNestedTypealiases {
            readonly __doNotUseOrImplementIt: {
                readonly "foo.SealedInterfaceWithNestedTypealiases": unique symbol;
            };
        }
        namespace SealedInterfaceWithNestedTypealiases {
            type SealedInterfaceNestedAlias = string;
            class Impl implements foo.SealedInterfaceWithNestedTypealiases {
                constructor();
                readonly __doNotUseOrImplementIt: foo.SealedInterfaceWithNestedTypealiases["__doNotUseOrImplementIt"];
            }
            namespace Impl {
                /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
                namespace $metadata$ {
                    const constructor: abstract new () => Impl;
                }
            }
        }
        class ClassWithIgnoredNestedTypealias {
            constructor();
        }
        namespace ClassWithIgnoredNestedTypealias {
            /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
            namespace $metadata$ {
                const constructor: abstract new () => ClassWithIgnoredNestedTypealias;
            }
            type VisibleAlias = number;
        }
        type MyInt = number;
        type MyString = string;
        type MyBoolean = boolean;
        type MyDouble = number;
        type MyByte = number;
        type MyShort = number;
        type MyFloat = number;
        type MyChar = any/* kotlin.Char */;
        type MyAny = any;
        type NullableInt = Nullable<number>;
        type NullableString = Nullable<string>;
        type NullableAny = Nullable<any>;
        type MyUByte = any/* kotlin.UByte */;
        type MyUShort = any/* kotlin.UShort */;
        type MyUInt = any/* kotlin.UInt */;
        type MyIntArray = Int32Array;
        type MyBooleanArray = any /*BooleanArray*/;
        type MyStringArray = Array<string>;
        type MyNullableIntArray = Nullable<Int32Array>;
        type NestedArray = Array<Array<string>>;
        type AliasSomeClass = foo.SomeClass;
        type AliasSomeInterface = foo.SomeInterface;
        type AliasSomeExternalInterface = foo.SomeExternalInterface;
        type AliasSomeEnum = foo.SomeEnum;
        type NullableSomeClass = Nullable<foo.SomeClass>;
        type ConcreteGenericClass = foo.GenericClass<string>;
        type ConcreteGenericClassInt = foo.GenericClass<number>;
        type ConcreteTwoGenericParamsClass = foo.TwoGenericParamsClass<string, number>;
        type AliasGenericClass<T> = foo.GenericClass<T>;
        type AliasTwoGenericParamsClass<A, B> = foo.TwoGenericParamsClass<A, B>;
        type FlippedTwoGenericParamsClass<A, B> = foo.TwoGenericParamsClass<B, A>;
        type PartiallySpecializedGenericClass<T> = foo.TwoGenericParamsClass<string, T>;
        type AliasClassWithConstraint<T> = foo.ClassWithConstraint<T>;
        type NullableGenericClass<T> = Nullable<foo.GenericClass<T>>;
        type GenericClassNullableParam<T> = foo.GenericClass<Nullable<T>>;
        type SimpleCallback = () => void;
        type IntToString = (p0: number) => string;
        type BinaryOperation = (p0: number, p1: number) => number;
        type GenericTransformer<T, R> = (p0: T) => R;
        type NullableCallback = Nullable<() => void>;
        type CallbackWithNullableParam = (p0: Nullable<number>) => Nullable<string>;
        type HigherOrderFunction = (p0: (p0: number) => string) => number;
        type CurriedFunction = (p0: number) => (p0: string) => boolean;
        type SuspendCallback = any /*Suspend functions are not supported*/;
        type SuspendTransformer<T, R> = any /*Suspend functions are not supported*/;
        type AliasOfMyInt = foo.MyInt;
        type AliasOfAliasSomeClass = foo.AliasSomeClass;
        type AliasOfGenericAlias<T> = foo.AliasGenericClass<T>;
        type MyThrowable = Error;
        type NullableThrowable = Nullable<Error>;
        type MyNothing = never;
        type MyUnit = void;
    }
}



G.DCL.01 避免遮盖（shadow）

【级别】建议

【描述】

由于变量、类型、函数、包名共享一个命名空间，这几类实体之间重用名字会发生遮盖 (shadow)，因此，需尽量避免无关的实体之间发生遮盖 (shadow)；

否则，这种源代码上的模糊性，会给维护者和检视者带来很大的困扰和负担。尤其是当代码既需要访问复用的标识符，又需要访问被覆盖的标识符时。当复用的标识符在不同的包里时，这个负担会变得更加沉重。

【反例】

```cangjie
main(): Unit {
    var name = ""
    var fn = {=>
        var name = "Zhang"  // Shadow
        println(name)
    }

    println(name)  // prints ""
    fn()  // prints "Zhang"
}
```
类似地，类型参数名称也要尽量避免遮盖。

【反例】

```cangjie
class Foo<T> {
    static func foo<T>(a: T): T { return a }

    func goo(a: T): T { return a }
}
```
上面代码中静态泛型函数的类型参数 T 遮盖了泛型类的类型参数 T。

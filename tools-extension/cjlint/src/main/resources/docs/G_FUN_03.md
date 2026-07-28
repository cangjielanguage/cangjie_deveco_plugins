G.FUN.03 避免在无关的函数之间重用名字，构成重载

【级别】建议

【描述】

函数重载的主要作用是使用同一个函数名来接受不同的参数实现相似的任务，为了代码的可读性和可维护性，应尽量避免完成不同任务的函数之间构成重载 (overload)。如果多个函数之间有必要构成重载，那么应满足以下要求：

* 它们应在同一个类型或文件内依次定义，避免重载的多个函数出现在不同作用域层级；
* 构成重载的函数之间应尽量避免同一组实参能通过多个函数的类型检查。

以上对函数重载的建议，指的是一个团队内部自定义的函数之间构成重载的建议，以下情形可以例外：

* 如果和第三方或标准库中的函数之间有必要构成重载，可以出现在不同包；
* 如果有必要进行操作符重载，操作符重载函数可以出现在不同文件或不同包；
* 父类和子类的函数之间如果有必要成重载，可以出现在不同文件或包。

【反例】

以下例子中，在 package a 定义了函数 fn(a: Derived)，在 package b 定义了 fn(a: Base) 的重载函数，由于两个重载函数在不同的作用域层级，导致在 package b 中调用 fn 时，根据作用域优先级原则，选择不是最匹配的 fn(a: Base)。

另一个不符合规范的例子是，两个构成重载的函数 g 的对应位置参数类型为被实例化的关系或父子类型关系，函数调用时两个函数均通过类型检查，但根据最匹配原则，没有最匹配函数，导致无法决议。

```cangjie
package a
public open class Base {
    ...
}
public class Derived <: Base {
    ...
}

public func fn(a: Derived) {
    ...
}

////
package b
import a.Base
import a.Derived
import a.fn

func fn(a: Base) { // 不符合：两个 fn 在不同的作用域层级
    ...
}

main() {
    fn(Derived()) // 根据作用域优先级原则，调用的是 fn(a: Base)
    g(Derived(), Derived()) // 根据最匹配原则，没有最匹配函数，无法决议
}

// 不符合: 两个 g 的对应位置参数类型为被实例化的关系或父子类型关系，很容易构造实参让两个均通过类型检查
func g<X>(a: X, b: Derived) {
    ...
}
func g(a: Derived, b: Base) {
    ...
}
```

【正例】

```cangjie
public open class Base {
    // CODE
}
public class Derived <: Base {
    // CODE
}

// 符合：构成重载的函数在同一层作用域内依次出现，且参数之间不存在子类型或被实例化的关系
func fn(a: Base) {
    // CODE
}
func fn(a: Int64) {
    // CODE
}
```
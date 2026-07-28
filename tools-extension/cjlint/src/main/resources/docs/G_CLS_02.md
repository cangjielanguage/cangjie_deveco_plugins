G.CLS.02 创建对象优先使用构造函数，慎用静态工厂方法

【级别】建议

【描述】

静态工厂方法是一种将对象的构造与使用相分离的设计模式，它使用一个（或一系列）静态函数代替构造函数来构造数据。

在仓颉中应优先使用构造函数来构造对象，除了下文提到的例外情况，尽量避免使用静态工厂方法。

首先，若必须使用，为了方便识别，静态工厂方法的名称应包含 from, of, valueOf, instance, get, new, create 等常用关键字来突出构造作用。 如果为了区别无法仅从参数中识别的多种构造方法，各个静态工厂函数的名称也应有所区别以解释具体的构造方式。

仓颉语言支持命名参数和操作符重载，在只用作区分构造方式的目的时，比起静态工厂方法， 应优先考虑带命名参数的构造函数、单位常量乘以数目等更直观的构造方式。在这些方法都难以理解时再考虑静态工厂方法。

【反例】

```cangjie
class Weight {
    private static const G_PER_KG = 1000.0
    let g: Int64

    Weight(gram: Int64, kilo: Float64) {
        g = gram + Int64(kilo * G_PER_KG)
    }
    init(gram: Int64) {
        this(gram, 0.0)
    }
    init(kilo: Float64) {
        this(0, kilo)
    }
}

// 不符合，难以区分
main() {
    let w1 = Weight(1)
    let w2 = Weight(0.5)
    let w3 = Weight(1, 2.0)
}

```
```cangjie
class Weight {
    private static const G_PER_KG = 1000.0
    private Weight(let g: Int64) {}

    static func ofGram(gram: Int64) {
        Weight(gram)
    }
    static func ofKilo(kilo: Float64) {
        Weight(Int64(kilo * G_PER_KG))
    }
    static func ofGramNKilo(gram: Int64, kilo: Float64) {
        Weight(gram + Int64(kilo * G_PER_KG))
    }
}

// 不符合，有更直观的替代方式
main() {
    let w1 = Weight.ofGram(1)
    let w2 = Weight.ofKilo(0.5)
    let w3 = Weight.ofGramNKilo(1, 2.0)
}

```

【正例】

```cangjie
class Weight {
    private static const G_PER_KG = 1000.0
    let g: Int64

    Weight(gram!: Int64, kilo!: Float64) {
        g = gram + Int64(kilo * G_PER_KG)
    }
    init(gram!: Int64) {
        this(gram: gram, kilo: 0.0)
    }
    init(kilo!: Float64) {
        this(gram: 0, kilo: kilo)
    }
}

/* 符合，优先使用 */
main() {
    let w1 = Weight(gram: 1)
    let w2 = Weight(kilo: 0.5)
    let w3 = Weight(gram: 1, kilo: 2.0)
}
```
```cangjie
class Weight {
    private static const G_PER_KG = 1000
    private Weight(let g: Int64) {}

    static let gram = Weight(1)
    static let kilo = Weight(G_PER_KG)

    operator func *(rhs: Int64) {
        Weight(g * rhs)
    }
    operator func *(rhs: Float64) {
        Weight(Int64(Float64(g) * rhs))
    }
    operator func +(rhs: Weight) {
        Weight(g + rhs.g)
    }
}

extend Int64 {
    operator func *(rhs: Weight) {
        rhs * this
    }
}

extend Float64 {
    operator func *(rhs: Weight) {
        rhs * this
    }
}

/* 符合，优先使用 */
main() {
    let w1 = 1 * Weight.gram
    let w2 = 0.5 * Weight.kilo
    let w3 = 1 * Weight.kilo + 2 * Weight.gram
}
```
```cangjie
import std.random.Random

class BigInteger {

    /* ....... */

    static func probablePrime(bitWidth: Int64, rnd: Random) {

        /* some complex computation */
        /* ............ */

        BigInteger()
    }
}

main() {
    // 符合，较为复杂，仅用命名参数难以解释清楚
    let rndPrime = BigInteger.probablePrime(16, Random())
}
```
另一种允许使用静态工厂方法的情况是在需要获得缓存的对象时，构造函数总是会构造新的对象，此时可以使用静态工厂方法来达到访问缓存的目的。 一些典型的情况包括：不可变类型、与资源绑定的类型、构造过程非常耗时的类型等。

【正例】

```cangjie
import std.collection.HashMap

class ImmutableData {
    private static let cache = HashMap<String, ImmutableData>()

    // 符合，返回缓存的不可变对象
    public static func getByName(name: String) {
        if (cache.contains(name)) {
            return cache[name]
        } else {
            cache[name] = ImmutableData(name)
            return cache[name]
        }
    }

    private ImmutableData(name: String) {
        // some very time-consuming process
    }
}

main() {
    let d1 = ImmutableData.getByName("abc") // new
    let d2 = ImmutableData.getByName("abc") // cached
}
```
最后一种情形是返回接口的实例从而将接口与实现类解耦，达到隐藏实现细节或者按需替换实现类的目的。

【正例】

```cangjie
sealed interface I1 {
    // 符合，隐藏实现细节
    static func getInstance() {
        return C1() as I1
    }
    func Safe():Unit
}

interface I2 <: I1 {
    func Safe():Unit
    func Secret():Unit
}

class C1 <: I2 {
    public func Safe():Unit {}
    public func Secret():Unit {}
}
```
```cangjie
sealed interface I1 {
    // 符合，根据输入选择实现方式
    static func fromInt(i: Int64) {
        if (i > 100) {
            return ImplForLarge() as I1
        } else {
            return ImplForSmall() as I1
        }
    }
    func foo():Unit
}

class ImplForLarge <: I1 {
    public func foo():Unit {}
}

class ImplForSmall <: I1 {
    public func foo():Unit {}
}
```

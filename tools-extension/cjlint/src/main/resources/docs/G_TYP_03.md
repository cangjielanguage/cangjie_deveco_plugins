G.TYP.03 判断变量是否为 NaN 时必须使用 isNaN() 方法

【级别】要求

【描述】

在仓颉语言中，NaN 是浮点类型的一个特殊值，它被用来表示非数值。

NaN 的独特之处在于它不等于任何值，包括它本身，与 NaN 进行比较的结果经常令人困惑，比如 NaN != NaN 的值是 true。因此，必须使用 Number.isNaN() 函数来测试一个值是否是 NaN。

【反例】

```cangjie
func test(x:Float64) {
    if (x != Float64.NaN) {
        println(x * 2)
    }
}
```

【正例】

```cangjie
func test(x:Float64) {
    if (!x.isNaN()) {
        println(x * 2)
    }
}
```
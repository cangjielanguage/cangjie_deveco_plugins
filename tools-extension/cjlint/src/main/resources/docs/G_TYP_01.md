G.TYP.01 确保以正确的策略处理除数

【级别】建议

【描述】

在除法运算和模运算中，可能会发生除数为 0 的错误。对于整数运算，仓颉在运行时会自动检查除数，当除数为 0 时会自动抛出异常。不处理除零的情况可能会导致程序终止或拒绝服务（DoS）。捕获除零异常可能会导致性能开销较高，存在多个除法操作的时候会导致难以排查异常抛出点，因此开发者需要显式地对除数进行判断。

【反例】

```cangjie
func f() {
    var num1: Int64
    var num2: Int64
    var result: Int64
    // Initialize num1 and num2
    ...
    result = num1 / num2
}
```
上面的示例中，有符号操作数 num1 和 num2 的除法运算，num2 可能为 0，导致除 0 错误的发生。

【正例】

```cangjie
func f() {
    var num1: Int64
    var num2: Int64
    var result: Int64
    // Initialize num1 and num2
    ...
    if (num2 == 0) {
        //Handle error
    } else {
        result = num1 / num2
    }
}
```
该正确示例中，对除数进行了检查，从而杜绝了发生除 0 错误的发生。

【反例】

```cangjie
func f() {
    var num1: Int64
    var num2: Int64
    var result: Int64
    // Initialize num1 and num2
    ...
    result = num1 % num2
}
```
整数类型的操作数，模运算符会计算除法中的余数。上述不符合规则的代码示例，在进行模运算时，可能会因为 num2 为 0 导致除 0 错误的发生。

【正例】

```cangjie
func f() {
    var num1: Int64
    var num2: Int64
    var result: Int64
    // Initialize num1 and num2
    ...
    if (num2 == 0) {
        //Handle error
    } else {
        result = num1 % num2
    }
}
```
该正确示例中，对除数进行了检查，从而杜绝了除 0 错误的发生。
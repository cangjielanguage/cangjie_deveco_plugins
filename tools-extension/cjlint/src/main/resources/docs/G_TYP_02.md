G.TYP.02 确保正确使用整数运算溢出策略

【级别】要求

【描述】

仓颉中提供三种属性宏来控制整数溢出的处理策略，@OverflowThrowing，@OverflowWrapping 和 @OverflowSaturating 分别对应抛出异常、高位截断以及饱和这三种溢出处理策略，默认情况下（即未使用宏），采取抛出异常的处理策略 。

实际情况下需要根据业务场景的需求正确选择溢出策略。例如要在 Int64 上实现某种安全运算，使得计算结果和计算过程在数学上相等，就需要使用抛出异常的策略。

【反例】

```cangjie
// 计算结果被高位截断
@OverflowWrapping
func operation(a: Int64, b: Int64): Int64 {
    a + b // No exception will be thrown when overflow occurs
}
```
该错误例子使用了高位截断的溢出策略，当传入的参数 a 和 b 太大时，可能产生高位截断的情况，导致计算结果和计算表达式 (a + b) 在数学上不是相等关系。

【正例】

```cangjie
// 安全
@OverflowThrowing
func operation(a: Int32, b: Int32): Int32 {
    a + b
}

func test(a: Int32, b: Int32) {
    try {
        let v = operation(a, b)
    } catch (e: ArithmeticException) {
        //Handle error
    }
}
```
该正确例子使用了抛出异常的溢出策略，当传入的参数 a 和 b 较大导致整数溢出时，operation 函数会抛出异常。

附录 B 总结了可能造成整数溢出的数学操作符。
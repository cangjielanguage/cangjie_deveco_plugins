G.FMT.07 一行只有一个声明或表达式

【级别】建议

【描述】声明或表达式应该单独占一行，更加利于阅读和理解代码。

【反例】

```cangjie
func foo() {
    // 不符合：多个变量声明需要分开放在多行
    var length = 0; var result = false

    // 不符合: 多个表达式需分开放在多行
    result = true; result
}
```

【正例】

```cangjie
func foo() {
    // 符合：多个变量声明需要分开放在多行
    var length = 0
    var result = false

    // 符合: 多个表达式分开放在多行
    result = true
    result
}
```

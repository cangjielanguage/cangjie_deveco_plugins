G.FMT.06 表达式中不要插入空白行

【级别】建议

【描述】

1. 当表达式过长，或者可读性不佳时，需要在合适的地方换行。表达式换行后的缩进要跟上一行的表达式开头对齐。
2. 可以在表达式中间插入注释行
3. 不要在表达式中间插入空白行

【正例】

```cangjie
func foo(s: String) {
    s
}

func bar(s: String) {
    s
}

/* 符合，表达式中可以插入注释进行说明 */
main() {
    let s = "Hello world"
        /* this is a comment */
        |> foo
        |> bar
}
```

【反例】

```cangjie
func foo(s: String) {
    s
}

func bar(s: String) {
    s
}

/* 不符合，表达式中不应插入空白行 */
main() {
    let s = "Hello world"
        |> foo


        |> bar
}
```

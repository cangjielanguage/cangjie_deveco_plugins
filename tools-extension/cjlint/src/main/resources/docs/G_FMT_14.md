G.FMT.14 代码注释放于对应代码的上方或右边

【级别】建议

【描述】

- 代码上方的注释与被注释的代码行间无空行，保持与代码一样的缩进。
- 代码右边的注释，与代码之间至少留有 1 个空格，且无需插入空格使其强行对齐。

  选择并统一使用如下风格：

    ```cangjie
    var foo = 100 // 放右边的注释
    var bar = 200 /* 放右边的注释 */
    ```

- 当右置的注释超过行宽时，请考虑将注释置于代码上方。同一个 block 中的代码注释不用插入空格使其强行对齐。

  【正例】

    ```cangjie
    class Column <: Div {
        var reverse: Bool = false
        var justifyContent: JustifyContent = JustifyContent.flexStart
        var alignItems: AlignItems = AlignItems.stretch  // 注释和代码间留一个空格
        var alignSelf: AlignSelf = AlignSelf.auto  // 上下两行注释无需插入空格强行对齐
        init() {
            ...
        }
    }
    ```

- `if else if` 为了更清晰，考虑将注释放在 `else if` 同行或者在块内，但不要放在 `else if` 之前，避免误解为注释是关于它所在块的。

  【反例】

    ```cangjie
    func test() {
        var nr = 100
        if (nr % 15 == 0) {
            println("fizzbuzz")
            // 当 nr 只能被 3 整除，不能被 5 整除不符合。
        } else if (nr % 3 == 0) {
            println("fizz")
        }
    }
    ```

  上述错误示例的注释是 `if` 分支的还是 `else if` 分支的，容易造成误解。

  【正例】

    ```cangjie
    func test() {
        var nr = 100
        if (nr % 15 == 0) {
            println("fizzbuzz")
        } else if (nr % 3 == 0) {
            // 当 nr 只能被 3 整除，不能被 5 整除不符合。
            println("fizz")
        }
    }
    ```

G.FMT.11 减少不必要的空行，保持代码紧凑

【级别】建议

【描述】

减少不必要的空行，可以显示更多的代码，方便代码阅读。建议：

- 根据上下内容的相关程度，合理安排空行；
- 类型定义和顶层函数定义与前后顶层元素之间至少空一行；
- 函数内部、类型定义内部、表达式内部，不使用连续空行；
- 不使用连续 3 个或更多空行；
- 大括号内的代码块行首之前和行尾之后不要加空行。

【反例】

```cangjie
class MyApp <: App {
    let album = albumCreate()
    let page: Router
    // 空行
    // 空行
    // 空行
    init() {           // 不符合：类型定义内部使用连续空行
        this.page = Router("album", album)
    }

    override func onCreate(): Unit {

        println( "album Init." )  // 不符合：大括号内部首尾存在空行

    }
}
```

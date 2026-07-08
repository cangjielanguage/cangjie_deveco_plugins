G.FMT.08 采用一致的空格缩进

【级别】建议

【描述】

建议使用空格进行缩进，每次缩进 4 个空格。避免使用制表符（`\t`）进行缩进。

对于 UI 等多层嵌套使用较多的产品，可以统一使用 2 个空格缩进。

【正例】

```cangjie
class ListItem  {
    var content: Array<Int64>   // 符合：相对类声明缩进 4 个空格
    init(
        content: Array<Int64>,  // 符合：函数参数相对函数声明缩进 4 个空格
        isShow!: Bool = true,
        id!: String = ""
    ) {
        this.content = content
    }
}
```

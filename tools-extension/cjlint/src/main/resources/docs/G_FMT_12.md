G.FMT.12 修饰符关键字按照一定优先级排列

【级别】建议

【描述】

以下是推荐的所有修饰符排列的优先级：

```cangjie
public/protected/private
open/abstract/static/sealed
override/redef
unsafe/foreign
const/mut
```

另外，因为 sealed 已经蕴含了 public 和 open 的语义，不推荐 sealed 与 public 或 open 同时使用。

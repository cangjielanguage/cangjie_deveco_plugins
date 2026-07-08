G.FMT.09 使用统一的大括号换行风格

【级别】建议

【描述】

选择并统一使用一种大括号换行风格，避免多种风格并存。

对于非空块状结构，大括号推荐使用 K&R 风格：

- 左大括号不换行；
- 右大括号独占一行，除非后面跟着同一表达式的剩余部分，如 `do-while` 表达式中的 `while`，或者 `if` 表达式中的 `else` 和 `else if` 等。

【正例】

```cangjie
enum TimeUnit {             // 符合：跟随声明放行末，前置 1 空格
    Year | Month | Day | Hour
}                           // 符合：右大括号独占一行

class A {                   // 符合：跟随声明放行末，前置 1 空格
    var count = 1
}

func fn(a: Int64): Unit {       // 符合：跟随声明放行末，前置 1 空格
    if (a > 0) {            // 符合：跟随声明放行末，前置 1 空格
        // CODE
    } else {                  // 符合：右大括号和 else 在同一行
        // CODE
    }                         // 符合：右大括号独占一行
}

// lambda 函数
let add = { base: Int64, bonus: Int64 =>     // 符合: lambda 表达式中非空块遵循 K&R 风格
    print("符合 news")
    base + bonus
}
```

【反例】

```cangjie
func fn(count: Int64)
{                           // 不符合：左大括号不应该单独一行
    if (count > 0)
    {                         // 不符合：左大括号不应该单独一行
        // CODE
    }                         // 不符合：右大括号后面还有跟随的 else，应该和 else 放同一行
    else {
        print("count <= 0")}        // 不符合：右大括号后面没有跟随的部分，应该独占一行
}
```

**例外：** 对于空块既可遵循前面的 K&R 风格，也可以在大括号打开后立即关闭，产品应考虑使用统一的风格。

【正例】

```cangjie
open class Demo {}    // 符合: 空块，左右大括号在同一行
```

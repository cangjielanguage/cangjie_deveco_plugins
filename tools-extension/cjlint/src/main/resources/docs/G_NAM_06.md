G.NAM.06 变量的名称采用小驼峰

【级别】建议

【描述】

变量、属性、函数参数、pattern 等均采用小驼峰命名风格。

例外：

- 泛型类型变量，允许单个大写字母，或单个大写字母加数字，或单个大写字母接下划线、大写字母和数字的组合，例如 E, T, T2, E_IN, E_OUT, T_CONS
- 函数内使用的数值常量，不要使用魔鬼数字，用 let 声明有意义的局部变量代替，此时局部变量名可以使用全大写下划线的风格命名，强调是常量

  不应该取 NUM_FIVE = 5 或 NUM_5 = 5 这样的 “魔鬼常量”。如果被粗心大意地改为 NUM_5 = 50 或 55 等，很容易出错。

【正例】

```cangjie
// 变量名使用小驼峰命名
let menuItems: Array<Item> = ...
let names: Array<String> = ...
let menuItemsArray: Array<Item> = ...
let menuItems: Set<Item> = ...
let rememberedSet: Set<Address> = ...
let waitingQue: Queue<Thread> = ...
let lookupTable: Array<Int64> = ...
let word2WordIdMap: Map<String, Int> = ...

class MyPage <: Page {
    var pageNo = StateInt64(1)                 // 实例成员变量使用小驼峰命名
    var imagePath = StateArray(images)         // 实例成员变量使用小驼峰命名
    init() {
        ...
    }
}

// 参数名使用小驼峰命名
func getColumnMoreDataColumn(pageType: String, idxColumn: Int64, outIndex: Int64) {
    ...
}

// 类型参数使用大写字母
class Map<KEY, VAL> { ... }

// 类型参数使用大写字母与数字
class Pair<T1, T2> { ... }
```

【反例】

```cangjie
// 不符合：变量名使用无意义单个字符
var i: Array<Item> = ...
// 不符合：类型参数使用小写字母
class Map<key, val> { ... }
```
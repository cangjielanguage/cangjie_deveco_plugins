G.FMT.02 一个源文件按顺序包含版权、package、import、顶层元素四类信息，且不同类别之间用空行分隔

【级别】建议

【描述】

一个源文件会包含以下几个可选的部分，应按顺序组织，且每个部分之间用空行隔开：

1. 许可证或版权信息；
2. package 声明，且不换行；
3. import 声明，且每个 import 不换行；
4. 顶层元素。

【正例】

```cangjie
// 第一部分，版权信息
/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

// 第二部分，package 声明
package com.huawei.myproduct.mymodule

// 第三部分，import 声明
import std.collection.HashMap   // 标准库

// 第四部分，public 元素定义
public class ListItem <: Component {
    // CODE
}

// 第五部分，internal 元素定义
class Helper {
    // CODE
}
```

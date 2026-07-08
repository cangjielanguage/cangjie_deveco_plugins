FFI.C.1 声明 struct 类型时，成员变量的顺序和类型要求和 C 语言侧保持一致

【级别】要求

【描述】

当使用 struct 来声明 C 语言中的结构体类型时，要求保持变量的顺序和类型一致。若没有保持一致，可能导致数据映射地址不正确，同时也可能因为类型不一致而出现截断错误。

进行结构体参数映射时，需要按照类型映射表来保证类型相匹配，具体参见附录 C 基础类型映射关系表。

【反例】

如下仓颉和 C 结构体中定义的前两个成员变量顺序不一致，导致类型大小顺序颠倒，类型对应不正确，在仓颉中能够使用 Int64 正常容纳的数据，映射到 C 语言的 int32_t 型，可能会出现截断错误。

```cangjie
// CTest.c
#include<stdio.h>
#include<stdint.h>
typedef struct {
    int32_t x; // int32_t 对应仓颉的 Int32
    int64_t y;
    int64_t z;
    int64_t a;
}CStruct;

void distance(CStruct cs) {
    printf("x=%d, y=%lld, z=%lld, a=%lld\n", cs.x, cs.y, cs.z, cs.a);
}
```
```cangjie
// CJTest.cj
foreign func distance(f: CStruct): Unit
@C
struct CStruct {
    var y: Int64  // 此处使用 Int64 对应 int32_t，不合法
    var x: Int32
    var z: Int64
    var a: Int64
    init(yy: Int64, xx: Int32, zz: Int64, aa: Int64) {
        y = yy
        x = xx
        z = zz
        a = aa
    }
}
```
按照如下给结构体赋值，第一个参数明显超出 Int32 最大范围，但没有超出 Int64 的范围，在仓颉中使用 Int64 可以正常使用，但映射到 C 语言中使用的是 int32_t 型接收，会出现截断错误。
```cangjie
main() {
    var y = CStruct(214748364888, 2147483647, 4, 8)
    print("yres:\n")
    unsafe { distance(y) }
}
```
```cangjie
yres:
x=88, y=140615081787391, z=4, a=8
```

【正例】

按照正确的对应顺序定义仓颉侧的 struct，则可以在编译时检查出数字范围溢出。
```cangjie
//CJTest.cj
foreign func distance(f: CStruct): Unit
@C
struct CStruct {
    var x: Int32
    var y: Int64
    var z: Int64
    var a: Int64
    init(xx: Int32, yy: Int64, zz: Int64, aa: Int64) {
        x = xx
        y = yy
        z = zz
        a = aa
    }
}

main() {
    var y = CStruct(214748364888, 2147483647, 4, 8)  // compiler will report error
    print("yres:\n")
    unsafe { distance(y) }
}
```
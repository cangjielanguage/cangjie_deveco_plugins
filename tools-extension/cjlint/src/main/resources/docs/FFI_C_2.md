FFI.C.2 foreign 声明的函数参数类型、参数数量和返回值类型要求和 C 语言侧对应的函数参数类型、参数数量和返回值类型保持一致

【级别】要求

【描述】

仓颉使用 foreign 声明 C 语言侧函数时应保持参数数量、参数类型、返回值类型严格一致。若参数数量不一致，仓颉这边传入的参数数量不够的话，可能导致 C 语言侧变量的值未被初始化而访问到随机值；若参数类型不一致，可能会导致参数传递过去后被截断；返回值类型不一致，可能会导致仓颉接收函数返回值时出现截断问题。

同样的，在使用 CFunc<T, T> 声明函数指针时，也需要保持参数类型和类型限定符一致，若不一致，则可能出现截断错误。

【反例】

函数指针接收时参数类型和类型限定符不一致可能导致截断。如下示例中，C 语言侧函数指针为 int16_t 型，仓颉为 Int32 型，传入的参数在 Int32 范围内，但超过了 int16_t 范围，会出现截断错误。

```cangjie
// CTest.c
#include<stdio.h>
#include<stdint.h>
typedef int16_t(*func_t)(int16_t, int16_t);

int16_t add(int16_t a, int16_t b) {
    int16_t sum = a + b;
    printf("%d + %d = %d\n", a, b, sum);
    return sum;
}

// Pass func ptr 'add' to CangJie.
func_t getFuncPtr() {
    printf("this is from getFuncPtr. addr: %d\n", &add);
    return add;
}
```
```cangjie
// CJTest.cj
foreign func getFuncPtr(): CFunc<(Int32, Int32) -> Int32>

main() {
    var add: CFunc<(Int32, Int32) -> Int32> = unsafe { getFuncPtr() }
    var bb = unsafe { add(214748364, 2) }
}
```
可以看到参数出现截断错误。
```cangjie
this is from getFuncPtr. addr: 575928392
-13108 + 2 = -13106
```

【正例】

仓颉侧和 C 语言侧类型保持一致，避免截断问题。
```cangjie
// CJTest.cj
foreign func getFuncPtr(): CFunc<(Int16, Int16) -> Int16>

main() {
    var add: CFunc<(Int16, Int16) -> Int16> = unsafe { getFuncPtr() }
    var bb = unsafe { add(214, 2) }
}
```
同时保持传参在类型大小范围内，将会正常执行。
```cangjie
this is from getFuncPtr. addr: -578103224
214 + 2 = 216
```

【反例】

参数类型不一致可导致截断。如下示例中，两侧互通函数 add 声明的参数类型不一致，传入后会发生截断。
```cangjie
//CTest.c
#include<stdio.h>
#include<stdint.h>
int add(short x, int y) { // 参数包含 short 型
    printf("x = %x, y = %x\n", x, y);
    return x + y;
}
```
```cangjie
// CJTest.cj
foreign func printf(fmt: CString, ...): Int32
foreign func add(x: Int32, y: Int32): Int32 // 参数全为 Int32 型

main() {
    var a: Int32 = 0x1234567
    var b: Int32 = 0
    var res: Int32 = unsafe { add(a, b) }
    unsafe {
        var cstr = LibC.mallocCString("res = %x \n")
        printf(cstr, res)
        LibC.free(cstr)
    }
}
```
运行结果如下，可以看到参数 x 传入后被截断，导致计算结果也被截断，仅保留了十六进制的低四位。
```cangjie
x = 4567, y = 0
res = 4567
```

【正例】

如下示例将互通函数两侧的参数都声明为 Int32 类型，避免截断问题。
```cangjie
// CTest.c
#include<stdio.h>
#include<stdint.h>
int add(int x, int y) {
    printf("x = %x, y = %x\n", x, y);
    return x + y;
}
```
```cangjie
// CJTest.cj
foreign func add(x: Int32, y: Int32): Int32
foreign func printf(fmt: CString, ...): Int32

main() {
    var a: Int32 = 0x1234567
    var b: Int32 = 0
    var res: Int32 = unsafe { add(a, b) }
    unsafe {
        var cstr = LibC.mallocCString("res = %x \n")
        printf(cstr, res)
        LibC.free(cstr)
    }
}
```

【反例】

参数数量不一致可导致访问任意值。互通函数两侧声明的参数数量不一致，会导致部分 C 侧变量没有得到初始化，从而访问到随机值。
```cangjie
// CTest.c
#include<stdio.h>
#include<stdint.h>
int add(int x, int y) {
    printf("x = %x, y = %x\n", x, y);
    return x + y;
}
```
```cangjie
// CJTest.cj
foreign func add(x: Int32): Int32
foreign func printf(fmt: CString, ...): Int32

main() {
    var a: Int32 = 123
    var res: Int32 = unsafe { add(a) } // 此处仅传递一个参数，第二个参数没有被初始化
    unsafe {
        var cstr = LibC.mallocCString("res = %d \n")
        printf(cstr, res)
        LibC.free(cstr)
    }
}
```
运行结果如下，可以看到 y 是一个未知值，导致结果也是一个随机值。
```cangjie
x = 123, y = 1439015064
res = 1439015187
```

【正例】

```cangjie
// CJTest.cj
foreign func add(x: Int32, y: Int32): Int32
foreign func printf(fmt: CString, ...): Int32

main() {
    var a: Int32 = 0x1234567
    var b: Int32 = 0
    var res: Int32 = unsafe { add(a, b) } // 此处正常传递两个参数
    unsafe {
        var cstr = LibC.mallocCString("res = %x \n")
        printf(cstr, res)
        LibC.free(cstr)
    }
}
```

【反例】

函数返回类型不一致可导致截断。

```cangjie
// CTest.c
#include<stdio.h>
#include<stdint.h>
int add(int x, int y) {
    printf("x = %x, y = %x\n", x, y);
    return x + y;
}
```
```cangjie
// CJTest.cj
foreign func printf(fmt: CString, ...): Int32
foreign func add(x: Int32, y: Int32): Int16 // 此处返回类型和 C 侧声明不一致，可能出现截断问题

main() {
    var a: Int32 = 0x12345
    var b: Int32 = 0
    var res: Int16 = unsafe { add(a, b) }
    unsafe {
        var cstr = LibC.mallocCString("res = %x \n")
        printf(cstr, res)
        LibC.free(cstr)
    }
}
```
运行结果如下，可以看到计算结果仅保留十六进制的低四位，发生了截断。
```cangjie
x = 12345, y = 0
res = 2345
```

【正例】

```cangjie
// CJTest.cj
foreign func printf(fmt: CString, ...): Int32
foreign func add(x: Int32, y: Int32): Int32 // 此处返回类型和 C 侧声明一致

main() {
    var a: Int32 = 0x12345678
    var b: Int32 = 0
    var res: Int32 = unsafe { add(a, b) }
    unsafe {
        var cstr = LibC.mallocCString("res = %x \n")
        printf(cstr, res)
        LibC.free(cstr)
    }
}
```
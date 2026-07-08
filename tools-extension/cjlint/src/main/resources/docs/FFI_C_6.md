FFI.C.6 外部数据作为 read() 和 write() 函数索引时必须确保在有效范围内

【级别】要求

【描述】

由于仓颉的 CPointer<T> 类的成员函数 read() 和 write() 支持设置索引，因此可能会使用来自外部的数据作为函数的索引。当使用外部数据作为函数索引时，需要确保其在索引的有效范围内, 否则可能会出现越界访问的风险。

【反例】

```cangjie
// CTest.c
#include<stdio.h>
#include<stdint.h>
#include<stdlib.h>
int* PassPointerToCangjie() {
    int *p = (int*)malloc(sizeof(int) * 5);
    if ( p == NULL) {
        return NULL;
    }
    for (int i = 0; i < 5; i++) {
        p[i] = i;
    }
    return p;
}

void GetPointerFromCangjie(int *a, int len)
{
    if ( a == NULL) {
        printf("Pointer a is null!\n");
        return;
    }
    for (int i = 0; i < len; i++) {
        printf("%d ", a[i]);
    }
}
```
```cangjie
// CJTest.cj
foreign func printf(fmt: CString, ...): Int32
foreign func PassPointerToCangjie(): CPointer<Int32>
foreign func GetPointerFromCangjie(a: CPointer<Int32>, len: Int32): Unit

func Foo(index: Int64) {
    var a: CPointer<Int32> = unsafe { PassPointerToCangjie() } // 接收的数组指针索引范围为 0-4
    if (a.isNull()) {
        return
    }
    var value = unsafe { LibC.mallocCString("%d\n") }
    unsafe { printf(value, a.read(index)) } // 此处 index 值为函数入参，有可能为外部输入数据
    unsafe { a.write(index, 123) } // 没有校验就直接作为数组索引，可能会导致越界访问
    var len: Int32 = 5
    unsafe { GetPointerFromCangjie(a, len) }
    unsafe { LibC.free(value) }
}

main() {
    var index: Int64 = 3
    Foo(index) //不会越界

    var index2: Int64 = 5
    Foo(index2) //发生越界
}
```

【正例】

```cangjie
// CJTest.cj
foreign func printf(fmt: CString, ...): Int32
foreign func PassPointerToCangjie(): CPointer<Int32>
foreign func GetPointerFromCangjie(a: CPointer<Int32>, len: Int32): Unit

let MAX: Int64 = 4

func Foo(index: Int64) {
    var a: CPointer<Int32> = unsafe { PassPointerToCangjie() } // 接收的数组指针索引范围为 0-4
    let value = unsafe { LibC.mallocCString("%d\n") }
    if (index < 0 || index> MAX) { // 对函数入参进行合理的校验
        return
    }
    unsafe { printf(value, a.read(index)) }
    unsafe { a.write(index, 123) }
    var len: Int32 = 5
    unsafe { GetPointerFromCangjie(a, len) }
    unsafe { LibC.free(value) }
}

main() {
    var index: Int64 = 3
    Foo(index) //不会越界

    var index2: Int64 = 5
    Foo(index2) //校验不通过，不会发生越界
}
```
【影响】未对外部数据中的整数值进行限制可能导致拒绝服务，缓冲区溢出，信息泄露，甚至执行任意代码。
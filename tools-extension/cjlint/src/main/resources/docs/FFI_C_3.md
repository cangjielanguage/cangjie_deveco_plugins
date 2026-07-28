FFI.C.3 仓颉侧接收 C 语言传递过来的指针时，如果可能接收到空指针，应在使用前检查是否为 NULL

【级别】要求

【描述】

仓颉编程语言提供 CPointer<T> 类型对应 C 语言的指针 T* 类型，CPointer<T> 可以使用类型名构造一个实例，用来接收 C 语言传递过来的指针类型，这个实例的值初始为空，相当于 C 语言的 NULL。如果传递过来的是空指针，则在仓颉侧接收到的也是空指针，没有校验就直接使用会造成空指针引用问题。

常见的场景：

1. 语言侧分配内存失败，返回空指针并传递过来；
2. 语言侧函数返回值为 NULL。

【反例】

没有处理空指针可导致程序崩溃。

```cangjie
//CTest.c
#include<stdio.h>
#include<stdint.h>
#include<stdlib.h>

int *PassInt32PointerToCangjie() {
    int *a = (int *)malloc(sizeof(int));
    if (a == NULL)
        return NULL;
    *a = 1234;
    return a;
}

void GetInt32PointerFromCangjie(int *a) {
    int b = 12;
    a = &b;
    printf("value of int *a = %d\n", *a);
}
```
```cangjie
//CJTest.cj
foreign func PassInt32PointerToCangjie(): CPointer<Int32>
foreign func GetInt32PointerFromCangjie(a: CPointer<Int32>): Unit

main() {
    var a = unsafe { PassInt32PointerToCangjie() } // 此处从 C 语言接收指针
    if (unsafe { a.read() != 2147483647 }) { // a 未校验就直接引用成员函数 read()，可能出现空指针引用
        return
    }
    unsafe { GetInt32PointerFromCangjie(a) }
}
```

【正例】

指针引用前先进行校验。

```cangjie
foreign func PassInt32PointerToCangjie(): CPointer<Int32>
foreign func GetInt32PointerFromCangjie(a: CPointer<Int32>): Unit

main() {
    var a = unsafe { PassInt32PointerToCangjie() } // 此处从 C 语言接收指针
    if (a.isNull()) { // 指针接收后先校验
        print("pointer is null!\n")
        return
    }
    if (unsafe { a.read() != 2147483647 }) { // a 未校验就直接引用成员函数 read()，可能出现空指针引用
        return
    }
    unsafe { GetInt32PointerFromCangjie(a) }
}
```
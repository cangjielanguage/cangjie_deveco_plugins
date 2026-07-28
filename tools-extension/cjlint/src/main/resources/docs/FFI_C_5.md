FFI.C.5 禁止访问已经释放过的资源

【级别】要求

【描述】

如果从 C 语言侧接收到的指针已经进行过释放操作，那么禁止在仓颉侧再次使用这些指针的值，也不得再引用负责接收这些指针的变量，否则可能会造成安全问题，如解引用已释放的内存的指针、再次释放这些指针的内存等。

再次使用已释放内存的指针，可能因为访问无效内存导致程序崩溃，建议在释放内存后将指针显式置空，在下次使用前进行判空校验。

【反例】

```cangjie
// CTest.c
#include<stdio.h>
#include<stdint.h>
#include<stdlib.h>
int* SetMem() {
    // 分配内存
    int* a = (int*)malloc(sizeof(int));
    *a = 123;
    return a;
}

void FreeMem(int* a) {
    // 释放内存
    if (a == NULL) {
        printf("Pointer a is NULL!\n");
        return;
    }
    free(a);
}
```
```cangjie
// CJTest.cj
foreign func SetMem(): CPointer<Int32>
foreign func FreeMem(a: CPointer<Int32>): Unit

var a = CPointer<Int32>()

func Foo() {
    a = unsafe { SetMem() }
    // 指针校验和其它操作
    unsafe { FreeMem(a) } // 调用 C 侧 free 之后指针实际不为空，a 为野指针
}

func Foo2() {
    if (!a.isNull()) { // 此处判空校验无效，会被绕过
        unsafe { a.read(0) } // 此处会被执行，访问非法地址
    }
}

main() {
    Foo()
    Foo2()
}
```

【正例】

```cangjie
// CJTest.cj
foreign func SetMem(): CPointer<Int32>
foreign func FreeMem(a: CPointer<Int32>): Unit

var a = CPointer<Int32>()

func Foo() {
    a = unsafe { SetMem() }
    // 使用指针
    unsafe { FreeMem(a) }
    a = CPointer<Int32>() // 建议使用完后将指针置为空，避免了使用已释放内存的问题。
}

func Foo2() {
    if (!a.isNull()) { // 此处校验有效，指针 a 为空，因此不会进入此分支，避免 use after free
        unsafe { a.read(0) }
    }
}

main() {
    Foo()
    Foo2()
}
```
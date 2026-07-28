FFI.C.4 资源不再使用时应予以关闭或释放

【级别】要求

【描述】

在仓颉和 C 语言交互时，可能会手动申请内存、句柄等系统资源，这些资源不再使用时应予以关闭或释放。

若需要分配或释放 C 侧的内存，需要在 C 语言侧提供内存分配和释放的接口，在仓颉侧调用对应的接口。若没有封装接口，则需要根据 C 语言规范要求，在 C 语言侧合理使用 free 或者 close 等函数进行释放。

如果是在仓颉侧直接调用 C 语言库函数分配内存，例如 LibC.malloc 等，如果分配内存成功，在使用完后也必须在仓颉侧调用 LibC.free 等内存释放函数来释放内存。

【反例】

仓颉侧自行分配和释放内存。下述示例代码中，使用完 CString 字符串，但之后没有调用相应的释放函数，导致内存泄漏。

```cangjie
foreign func printf(fmt: CString, ...): Int32

main() {
    var str = unsafe { LibC.mallocCString("hello world!\n") }
    unsafe { printf(str) }
    // 使用完后没有释放 str 的内存
}
```

【正例】

下述示例中，使用完 CString 字符串后及时调用 LibC.free 来释放内存，消除了上述风险。

```cangjie
foreign func printf(fmt: CString, ...): Int32

main() {
    var str = unsafe { LibC.mallocCString("hello world!\n") }
    unsafe { printf(str) }
    unsafe { LibC.free(str) }    // 使用完后释放内存
}
```

【反例】

若 C 侧提供内存释放函数，则需要在仓颉侧进行调用来释放内存。

```cangjie
// CTest.c
#include<stdio.h>
#include<stdint.h>
#include<stdlib.h>
int* SetMem() {
    // 分配内存
    int* a = (int*)malloc(sizeof(int));
    if (a == NULL) {
        return NULL;
    }
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

main() {
    var a: CPointer<Int32> = unsafe { SetMem() }
    // do something
    // 此处函数直接返回，未调用 C 侧释放函数来释放之前分配的内存
}
```

【正例】

```cangjie
// CJTest.cj
foreign func SetMem(): CPointer<Int32>
foreign func FreeMem(a: CPointer<Int32>): Unit

main() {
    var a: CPointer<Int32> = unsafe { SetMem() }
    // do something
    unsafe { FreeMem(a) } // 使用完后及时释放内存
    a = CPointer<Int32>() // 将 a 置为空
}
```

【影响】如果资源在结束使用前未正确地关闭或释放，会造成系统的内存泄漏、句柄泄漏等资源泄漏漏洞。如果攻击者可以有意触发资源泄漏，则可能能够通过耗尽资源来发起拒绝服务攻击。
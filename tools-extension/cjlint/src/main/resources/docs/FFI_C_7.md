FFI.C.7 强制进行指针类型转换时避免出现截断错误

【级别】要求

【描述】

仓颉中的不同指针类型间相互进行强制转换时，需要注意强制类型转换前后内存中的数据是不变的，但可能出现元素的合并和拆分的情况，元素个数也可能因此发生变化，使用者必须充分了解数据的内存分布情况，否则不要使用强制指针类型转换。

【反例】

如下示例，将 Int32 类型指针强制转换成 Int16 型，会将数据截断为低两位和高两位，但内存中的数据实际并没有变化，可以通过成员函数 read() 访问，如 read(0) 访问低两位数据，read(1) 访问高两位数据，元素个数由原来的一个变成了两个，并且都可以通过索引访问到内存，但访问第二个元素的时候实际上是越界访问内存。

```cangjie
// CTest.c
#include<stdio.h>
#include<stdint.h>
#include<stdlib.h>
int *PassPointerToCangjie() {
    int *p = (int *)malloc(sizeof(int));
    if (p == NULL)
        return NULL;
    *p = 0x1234;
    return p;
}
```
```cangjie
foreign func printf(fmt: CString, ...): Int32
foreign func PassPointerToCangjie(): CPointer<Int32>

main() {
    var a: CPointer<Int32> = unsafe { PassPointerToCangjie() }
    var b: CPointer<Int16> = CPointer<Int16>(a) // 此处将 Int32 类型指针强制转换成 Int16 型
    if (b.isNull()) {
        print("pointer is null!\n")
        return
    }
    if (unsafe { b.read() != 0 }) {
        print("Pointer was cut!\n")
        var value = unsafe { LibC.mallocCString("%x\n") }
        unsafe { printf(value, b.read(1)) } // read(1) 访问 Int16 的高两位数据，可能造成越界访问
        unsafe { LibC.free(value) }
        return
    }
    var value = unsafe { LibC.mallocCString("%x\n") }
    unsafe { printf(value, b.read(0)) }
    unsafe { LibC.free(value) }
}
```

【正例】

谨慎使用强制指针类型转换。

```cangjie
foreign func printf(fmt: CString, ...): Int32
foreign func PassPointerToCangjie(): CPointer<Int32>

main() {
    var a: CPointer<Int32> = unsafe { PassPointerToCangjie() }
    // 删除此处的强制类型转换
    if (a.isNull()) {
        print("pointer is null!\n")
        return
    }
    if (unsafe { a.read() != 0 }) {
        print("Pointer a was cut!\n")
        var value = unsafe { LibC.mallocCString("%x\n") }
        unsafe { printf(value, a.read(0)) }
        unsafe { LibC.free(value) }
        return
    }
    var value = unsafe { LibC.mallocCString("%x\n") }
    unsafe { printf(value, a.read(0)) }
    unsafe { LibC.free(value) }
}
```
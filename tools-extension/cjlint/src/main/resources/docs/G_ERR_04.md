G.ERR.04 不要使用 return、break、continue 或抛出异常使 finally 块非正常结束

【级别】要求

【描述】

在 finally 代码块中，直接使用 return、break、continue、throw 语句，或调用一个函数而未处理其抛出的异常，会导致 finally 代码块无法正常结束。非正常结束的 finally 代码块会影响 try 或 catch 代码块中异常的抛出，也可能会影响当前函数的返回值，导致程序的逻辑难以理解。所以要保证 finally 代码块正常结束。

【反例】

```cangjie
func foo() {
    while(true) {
        try {
            // ...
            return 1
        } catch(err) {
            // ...
            return 2
        } finally {
            break // will break instead of return
        }
    }
    return 3 // will return 3
}
```
在该反例中，finally 块中的 break 对控制流的影响会遮盖 try 或 catch 中的 return，导致该 try-catch-finally 块实际上执行结束后会跳出外层的 while 循环，而不是让整个函数返回，该函数实际上可能返回 3。这样的代码很有迷惑性。

【正例】

```cangjie
func foo() {
    while(true) {
        try {
            // ...
            return 1 // may return 1
        } catch(err) {
            // ...
            return 2 // may return 2
        } finally {
            println("finished")
        }
    }
    return 3
}
```
在该正例中，如果 try 或 catch 块正常执行结束，则总是会执行返回语句，不会被 finally 块影响。
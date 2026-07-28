G.CON.05 避免使用不正确形式的双重锁定检查

【级别】建议

【描述】

双重锁定（double-checked locking idiom）是一种软件设计模式，通常用于延迟初始化单例。主要通过在进行获取锁之前先检查单例对象是否创建（第一次检查），在获取锁以后，再次检查对象是否创建（第二次检查），以此减少并发获取锁的开销。

但是取决于具体实现的内存模型，不正确的双重锁定检查使用可能会导致一个未初始化或部分初始化的对象对其它线程可见。因此只有在能为实例的完整构建建立正确的 happens-before 关系的情况下，才可以使用双重锁定检查。

【反例】

```cangjie
import std.sync.*

class Foo {
    private var helper: Option<Helper> = None<Helper>
    private let mtx: ReentrantMutex = ReentrantMutex()

    public func getHelper(): Helper {
        match (helper) {
            case None =>
                synchronized(mtx) {
                    match (helper) {
                        case None =>
                            let temp = Helper()
                            helper = Some<Helper>(temp)
                            return temp
                        case Some(h) => return h
                    }
                }
            case Some(h) => return h
        }
    }
}
```
上述错误示例中，使用了双重锁定检查的错误形式。对 Helper 对象进行初始化的写入和对 Helper 数据成员的写入，可能不按次序进行或完成。因此，一个调用 getHelper() 的线程可能会得到指向一个 helper 对象的非空引用，但该对象的数据成员为默认值而不是构造函数中设置的值。

【正例】

```cangjie
import std.sync.*

class Foo {
    private var helper = AtomicOptionReference<Helper>()
    private let mtx: ReentrantMutex = ReentrantMutex()

    public func getHelper(): Helper {
        match (helper.load()) {
            case None =>
                synchronized(mtx) {
                    match (helper.load()) {
                        case None =>
                            let temp = Helper()
                            helper = AtomicOptionReference<Helper>(temp)
                            return temp
                        case Some(h) => return h
                    }
                }
            case Some(h) => return h
        }
    }
}
```
上述例子将使用 AtomicReference 类对 Helper 的使用进行了封装，该类型会禁用编译优化，使得对 helper 对象的操作满足 happens-before 关系。

【正例】

```cangjie
class Foo {
    private static let helper: Helper = Helper()

    public static func getHelper(): Helper {
        return helper
    }
}
```
上述正确示例中，在对静态变量的声明中完成了 helper 字段的初始化。但是该实例没有使用延迟初始化。

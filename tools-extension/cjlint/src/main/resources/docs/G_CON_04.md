G.CON.04 避免在产生阻塞操作中持有锁

【级别】要求

【描述】

在耗时严重或阻塞的操作中持有锁，可能会严重降低系统的性能。另外，无限期的阻塞相互关联的线程，会导致死锁。阻塞操作一般包括：网络、文件和控制台 I/O 等，将一个线程延时同样会形成阻塞操作。所以程序持有锁时应避免执行这些操作。

【反例】

```cangjie
import std.sync.*
import std.time.*

let mtx: MultiConditionMonitor = MultiConditionMonitor()
let c: ConditionID = mtx.newCondition()

func doSomething(time: Duration) {
    synchronized(mtx) {
        sleep(time)
    }
}
```

上述错误示例中，doSomething() 函数是同步的，当线程挂起时，其他线程也不能使用该同步函数。

【正例】

```cangjie
import std.sync.*
import std.time.*

let mtx: MultiConditionMonitor = MultiConditionMonitor()
let c: ConditionID = synchronized(mtx) {
    mtx.newCondition()
}

func doSomething(timeout: Duration) {
    synchronized(mtx) {
        while (/* waiting for something */) {
            mtx.wait(c, timeout: timeout) // Immediately releases the current mutex
        }
    }
}
```

上述正确示例中，使用 mtx 对象的 wait 函数设置一个 timeout 期限并阻塞当前线程，然后将 mtx 对象锁释放。在 timeout 期限到达或者该线程被 mtx 对象的 notify() 或 notifyAll() 函数唤起时，该线程会重新尝试获取 mtx 锁。

【反例】

```cangjie
import std.sync.*
// Class Page is defined separately.
// It stores and returns the Page name via getName()
let pageBuff: Array<Page> = Array<Page>(MAX_PAGE_SIZE) { i => Page() }
let mtx = ReentrantMutex()

public func sendPage(socket: Socket, pageName: String): Bool {
    synchronized(mtx) {
        var write_bytes: Option<Int64>
        var targetPage = None<Page>
        // Send the Page to the server
        for (p in pageBuff) {
            match (p.getName().compareTo(pageName)) {
                case EQUAL => targetPage = Some<Page>(p)
                case _ => ...
            }
        }
        // Requested Page does not exist
        match (targetPage) {
            case None => return false
            case _ => ...
        }
        // Send the Page to the client
        // (does not require any synchronization)
        write_bytes = socket.write(targetPage.getOrThrow().getBuff())
        ...
    }
    return true
}
```
上述错误示例中，sendPage() 函数会从服务器发送一个 page 对象的数据到客户端。当多个线程并发访问时，同步函数会保护 pageBuf 队列，而 writeObject() 操作会导致延时，在高延时的网络或当网络条件本身存在丢包时，该锁会被长期无意义地持有。

【正例】

```cangjie
import std.sync.*
// Class Page is defined separately.
// It stores and returns the Page name via getName()
// let pageBuff: Array<Page> = Array<Page>(MAX_PAGE_SIZE) { i => Page() }
let pageBuff = ArrayList<Page>()
let mtx = ReentrantMutex()

public func sendPage(socket: Socket, pageName: String): Bool {
    let targetPage = getPage(pageName)
    match (targetPage) {
        case None => return false
        case _ => ...
    }
    // Send the Page to the client
    // (does not require any synchronization)
    deliverPage(socket, targetPage.getOrThrow())
    ...
    return true
}

// Requires synchronization
private func getPage(pageName: String): Option<Page> {
    synchronized(mtx) {
        var targetPage = None<Page>
        for (p in pageBuff) {
            match (p.getName().compareTo(pageName)) {
                case EQUAL => targetPage = Some<Page>(p)
                case _ => ...
            }
        }
        return targetPage
    }
}

private func deliverPage(socket: Socket, targetPage: Page) {
    var write_bytes: Option<Int64>
    // Send the Page to the client
    // (does not require any synchronization)
    write_bytes = socket.write(targetPage.getBuff())
    ...
}
```
上述正确示例中，将原来的 sendPage() 分为三个具体步骤执行，不同步的 sendPage() 函数调用同步的 getPage() 函数来在 pageBuff 队列中获得请求的 page。在取得 page 后，会调用不同步的 deliverPage() 函数类提交 page 到客户端。

例外场景：

向调用者提供正确终止阻塞操作的类可不遵守该要求。
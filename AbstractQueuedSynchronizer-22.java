import java.util.concurrent.locks.AbstractOwnableSynchronizer;

/***
 * 【参考资料】
 * 深入剖析基于并发AQS的(独占锁)重入锁(ReetrantLock)及其Condition实现原理：
 * https://blog.csdn.net/javazejian/article/details/75043422
 * 
 * 
 * AbstractQueuedSynchronizer又称为队列同步器(后面简称AQS)，它是用来构建锁或其他同步组件的基础框架，
 * 内部通过一个int类型的成员变量state来控制同步状态,
 * 当state=0时，则说明没有任何线程占有共享资源的锁，
 * 当state=1时，则说明有线程目前正在使用共享变量，其他线程必须加入同步队列进行等待。
 * 
 * AQS内部通过内部类Node构成FIFO的同步队列来完成线程获取锁的排队工作，同时利用内部类ConditionObject构建等待队列，
 * 当Condition调用wait()方法后，线程将会加入等待队列中，
 * 而当Condition调用signal()方法后，线程将从等待队列转移动同步队列中进行锁竞争。
 * 注意这里涉及到两种队列，一种的同步队列，当线程请求锁而等待的后将加入同步队列等待，
 * 而另一种则是等待队列(可有多个)，通过Condition调用await()方法释放锁后，将加入等待队列。
 * 
 */

 /**
  * 【参考资料】
 * AQS抽象类相关链接
 * https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/AbstractQueuedSynchronizer.html
 * https://baike.baidu.com/item/AbstractQueuedSynchronizer/10627013?fr=aladdin
 */


// 【嵌套类摘要】
class AbstractQueuedSynchronizer.ConditionObject
// AbstractQueuedSynchronizer 的 Condition 实现是 Lock 实现的基础。


// 【构造方法摘要】
protected AbstractQueuedSynchronizer()
// 使用初始同步状态 0 来创建一个新的 AbstractQueuedSynchronizer 实例。


// 【方法摘要】
void acquire(int arg)
// 以独占模式获取对象，忽略中断。

void acquireInterruptibly(int arg)
// 以独占模式获取对象，如果被中断则中止。

void acquireShared(int arg)
// 以共享模式获取对象，忽略中断。

void acquireSharedInterruptibly(int arg)
// 以共享模式获取对象，如果被中断则中止。

protected boolean compareAndSetState(int expect,int update)
// 如果当前状态值等于预期值，则以原子方式将同步状态设置为给定的更新值。

Collection<Thread> getExclusiveQueuedThreads()
// 返回包含可能正以独占模式等待获取的线程 collection。

Thread getFirstQueuedThread()
// 返回队列中第一个（等待时间最长的）线程，如果目前没有将任何线程加入队列，则返回 null。

Collection<Thread> getQueuedThreads()
// 返回包含可能正在等待获取的线程 collection。

int getQueueLength()
// 返回等待获取的线程数估计值。

Collection<Thread> getSharedQueuedThreads()
// 返回包含可能正以共享模式等待获取的线程 collection。

protected int getState()
// 返回同步状态的当前值。

Collection<Thread> getWaitingThreads(AbstractQueuedSynchronizer.ConditionObject condition)
// 返回一个 collection，其中包含可能正在等待与此同步器有关的给定条件的那些线程。

int getWaitQueueLength(AbstractQueuedSynchronizer.ConditionObject condition)
// 返回正在等待与此同步器有关的给定条件的线程数估计值。

boolean hasContended()
// 查询是否其他线程也曾争着获取此同步器；也就是说，是否某个 acquire 方法已经阻塞。

boolean hasQueuedThreads()
// 查询是否有正在等待获取的任何线程。

boolean hasWaiters(AbstractQueuedSynchronizer.ConditionObject condition)
// 查询是否有线程正在等待给定的、与此同步器相关的条件。

protected boolean isHeldExclusively()
// 如果对于当前（正调用的）线程，同步是以独占方式进行的，则返回 true。

boolean isQueued(Thread thread)
// 如果给定线程目前已加入队列，则返回 true。

boolean owns(AbstractQueuedSynchronizer.ConditionObject condition)
// 查询给定的 ConditionObject 是否使用了此同步器作为其锁定。

boolean release(int arg)
// 以独占模式释放对象。

boolean releaseShared(int arg)
// 以共享模式释放对象。

protected void setState(int newState)
// 设置同步状态的值。

String toString()
// 返回标识此同步器及其状态的字符串。

protected boolean tryAcquire(int arg)
// 试图在独占模式下获取对象状态。

boolean tryAcquireNanos(int arg,long nanosTimeout)
// 试图以独占模式获取对象，如果被中断则中止，如果到了给定超时时间，则会失败。

protected int tryAcquireShared(int arg)
// 试图在共享模式下获取对象状态。

boolean tryAcquireSharedNanos(int arg,long nanosTimeout)
// 试图以共享模式获取对象，如果被中断则中止，如果到了给定超时时间，则会失败。

protected boolean tryRelease(int arg)
// 试图设置状态来反映独占模式下的一个释放。

protected boolean tryReleaseShared(int arg)
// 试图设置状态来反映共享模式下的一个释放。

// 【从类 java.lang.Object 继承的方法】
clone,equals,finalize,getClass,hashCode,notify,notifyAll,wait,wait,wait

// 【构造方法详细信息】
AbstractQueuedSynchronizer
protected AbstractQueuedSynchronizer（）
// 使用初始同步状态 0 来创建一个新的 AbstractQueuedSynchronizer 实例。

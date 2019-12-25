package java.util.concurrent.locks;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import jdk.internal.vm.annotation.ReservedStackAccess;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject;

/**
 * 可重入互斥锁，其基本行为和语义与使用同步方法和语句访问的隐式监视锁相同，但具有扩展功能。
 */
public class ReentrantLock implements Lock, java.io.Serializable {
    private static final long serialVersionUID = 7373984872572414699L;
    /** 提供所有实现机制的同步器 */
    private final Sync sync;

    /**
     * 此锁的同步控制基础。子类到下面的公平和非空中版本。使用AQS状态表示锁上的保留数。
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -5179523762034025860L;

        /**
         * 不公平的trylock。tryAcquire在子类中实现，但两者都需要trylock方法的非空try。
         */
        @ReservedStackAccess
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }

        @ReservedStackAccess
        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }

        protected final boolean isHeldExclusively() {
// 虽然我们必须在所有者之前处于一般读取状态，但不需要这样做来检查当前线程是否为所有者
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        // 从外部类中继的方法

        final Thread getOwner() {
            return getState() == 0 ? null : getExclusiveOwnerThread();
        }

        final int getHoldCount() {
            return isHeldExclusively() ? getState() : 0;
        }

        final boolean isLocked() {
            return getState() != 0;
        }

        /**
         * 从输入流重建实例（即反序列化实例）。
         */
        private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            setState(0); // 重置到解锁状态
        }
    }

    /**
     * 非公平锁的同步对象
     */
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = 7316153563782823691L;
        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    /**
     * 公平锁的同步对象
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;
        /**
         * 公平的tryAcquire版本。除非递归调用或没有服务生或是第一个，否则不要授予访问权限。
         */
        @ReservedStackAccess
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }

    /**
     * 创建ReentrantLock的实例。
     */
    public ReentrantLock() {
        sync = new NonfairSync();
    }

    /**
     * 使用给定的公平性策略创建ReentrantLock的实例。
     */
    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }

    /**
     * 获取锁。
     */
    public void lock() {
        sync.acquire(1);
    }

    /**
     * 获取锁，除非当前线程被中断。
     */
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    /**
     * 只有在调用时另一个线程未持有锁时，才获取锁。
     */
    public boolean tryLock() {
        return sync.nonfairTryAcquire(1);
    }

    /**
     * 如果在给定的等待时间内没有被另一个线程持有并且当前线程没有被中断，则获取锁
     */
    public boolean tryLock(long timeout, TimeUnit unit)
            throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    /**
     * 试图释放此锁。
     */
    public void unlock() {
        sync.release(1);
    }

    /**
     * 返回与此Lock实例一起使用的实例。
     */
    public Condition newCondition() {
        return sync.newCondition();
    }

    /**
     * 查询当前线程对此锁的保留数。
     */
    public int getHoldCount() {
        return sync.getHoldCount();
    }

    /**
     * 查询此锁是否由当前线程持有。
     */
    public boolean isHeldByCurrentThread() {
        return sync.isHeldExclusively();
    }

    /**
     * 查询此锁是否由任何线程持有。此方法设计用于监视系统状态，而不是用于同步控制。
     */
    public boolean isLocked() {
        return sync.isLocked();
    }

    /**
     * 如果此锁的公平性设置为true，则返回 true。
     */
    public final boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
     * 返回当前拥有此锁的线程（如果不拥有）。
     * 当该方法由不是所有者的线程调用时，返回值反映了当前锁定状态的尽力而为的近似值。
     * 例如，即使有线程试图获取锁，但尚未获取锁，所有者也可能是暂时的。
     * 此方法旨在促进提供更广泛锁的子类的构造监测设施。
     */
    protected Thread getOwner() {
        return sync.getOwner();
    }

    /**
     * 查询是否有线程正在等待获取此锁。
     * 请注意，由于取消操作可能随时发生，因此返回不保证任何其他线程将获得此锁。
     * 此方法主要用于监视系统状态。
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**
     * 查询给定线程是否正在等待获取此锁。
     * 注意，由于取消可能随时发生，因此返回不保证此线程将获得此锁。
     * 此方法主要用于监视系统状态。
     */
    public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
    }

    /**
     * 返回等待获取此锁的线程数的估计值。
     * 该值只是一个估计值，因为当此方法遍历内部数据结构时，线程数可能会动态更改。
     * 该方法设计用于监控系统状态，不用于同步控制。
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     * 返回包含可能正在等待获取此锁的线程的集合。
     * 由于实际的线程集在构造此结果时可能会动态更改，因此返回的集合只是一个最佳努力估计值。
     * 返回集合的元素没有特定的顺序。此方法旨在促进子类的构造，从而提供更广泛的监视设施。
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    /**
     * 查询是否有任何线程正在等待与此锁关联的给定条件。
     * 注意，由于超时和中断可能随时发生，返回不能保证将来的信号将唤醒任何线程。
     * 此方法主要用于监视系统状态。
     */
    public boolean hasWaiters(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.hasWaiters((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     * 返回与此锁关联的给定条件上等待的线程数的估计值。
     * 请注意，由于超时和中断可能随时发生，估计值仅用作实际服务生人数的上限。
     * 此方法设计用于监视系统状态，而不是用于同步控制。
     */
    public int getWaitQueueLength(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     * 返回一个集合，其中包含可能正在等待与此锁关联的给定条件的线程。
     * 由于实际的线程集在构造此结果时可能会动态更改，因此返回的集合只是一个最佳努力估计值。
     * 返回集合的元素没有特定的顺序。此方法旨在促进子类的构建，从而提供更广泛的状态监视设施。
     */
    protected Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     * 返回标识此锁及其锁状态的字符串。
     */
    public String toString() {
        Thread o = sync.getOwner();
        return super.toString() + ((o == null) ?
                                   "[Unlocked]" :
                                   "[Locked by thread " + o.getName() + "]");
    }
}

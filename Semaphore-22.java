/*
 * 【参考链接】
 * 2019年12月25日09:35:01
 * https://tool.oschina.net/uploads/apidocs/jdk-zh/java/util/concurrent/Semaphore.html
 * 
 */

package java.util.concurrent;
import java.util.Collection;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Semaphore类是一个计数信号量，必须由获取它的线程释放，通常用于限制可以访问某些资源（物理或逻辑的）线程数目，信号量控制的是线程并发的数量。
 * 计数器：一个信号量有且仅有3种操作，且它们全部是原子的：初始化、增加和减少
 * 增加可以为一个进程解除阻塞；
 * 减少可以让一个进程进入阻塞。
 * 
 * 原理理解：
 * Semaphore是用来保护一个或者多个共享资源的访问，Semaphore内部维护了一个计数器，其值为可以访问的共享资源的个数。
 * 一个线程要访问共享资源，先获得信号量，如果信号量的计数器值大于1，意味着有共享资源可以访问，则使其计数器值减去1，再访问共享资源。
 * 如果计数器值为0,线程进入休眠。当某个线程使用完共享资源后，释放信号量，并将信号量内部的计数器加1，之前进入休眠的线程将被唤醒并再次试图获得信号量。
 * 
 */
public class Semaphore implements java.io.Serializable {
    private static final long serialVersionUID = -3222578661600680210L;
    /**通过AbstractQueuedSynchronizer子类的所有机制  */
    private final Sync sync;

    /**
     * 信号量的同步实现。使用AQS状态表示许可证。分为公平和非公平版本。
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1192457210091910933L;

        Sync(int permits) {
            setState(permits);
        }

        final int getPermits() {
            return getState();
        }

        final int nonfairTryAcquireShared(int acquires) {
            for (;;) {
                int available = getState();
                int remaining = available - acquires;
                if (remaining < 0 ||
                    compareAndSetState(available, remaining))
                    return remaining;
            }
        }

        protected final boolean tryReleaseShared(int releases) {
            for (;;) {
                int current = getState();
                int next = current + releases;
                if (next < current) // overflow
                    throw new Error("Maximum permit count exceeded");
                if (compareAndSetState(current, next))
                    return true;
            }
        }

        final void reducePermits(int reductions) {
            for (;;) {
                int current = getState();
                int next = current - reductions;
                if (next > current) // underflow
                    throw new Error("Permit count underflow");
                if (compareAndSetState(current, next))
                    return;
            }
        }

        final int drainPermits() {
            for (;;) {
                int current = getState();
                if (current == 0 || compareAndSetState(current, 0))
                    return current;
            }
        }
    }

    /**
     * 不公平版本
     */
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = -2694183684443567898L;

        NonfairSync(int permits) {
            super(permits);
        }

        protected int tryAcquireShared(int acquires) {
            return nonfairTryAcquireShared(acquires);
        }
    }

    /**
     * 公平版本
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = 2014338818796000944L;

        FairSync(int permits) {
            super(permits);
        }

        protected int tryAcquireShared(int acquires) {
            for (;;) {
                if (hasQueuedPredecessors())
                    return -1;
                int available = getState();
                int remaining = available - acquires;
                if (remaining < 0 ||
                    compareAndSetState(available, remaining))
                    return remaining;
            }
        }
    }

    /**
     * 创建具有给定许可数和非空公平设置的信号量。
     */
    public Semaphore(int permits) {
        sync = new NonfairSync(permits);
    }

    /**
     * 创建具有给定许可数和给定公平性设置的信号量。
     */
    public Semaphore(int permits, boolean fair) {
        sync = fair ? new FairSync(permits) : new NonfairSync(permits);
    }

    /**
     * 从这个信号量获取一个许可，阻塞直到一个可用，或者线程被中断。
     */
    public void acquire() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * 从这个信号量获取一个许可，阻塞直到一个可用为止。
     * 获得许可证，如果有许可证并立即返回，将可用许可证的数量减少一个。
     *
     * 如果没有可用的许可证，则当前线程将被禁用以进行线程调度，并处于休眠状态，
     * 直到其他某个线程调用此信号量的释放方法，并且下一个将为当前线程分配许可证。
     */
    public void acquireUninterruptibly() {
        sync.acquireShared(1);
    }

    /**
     * 从该信号量获取一个许可证，前提是在调用时有一个许可证可用。
     * 获取许可证（如果有）并立即返回，值为true，将可用许可证的数量减少一个。
     * 如果没有可用的许可证，则此方法将立即返回值false。
     */
    public boolean tryAcquire() {
        return sync.nonfairTryAcquireShared(1) >= 0;
    }

    /**
     * 从该信号量获取许可证，前提是在给定的等待时间内该信号量可用并且当前线程未被中断。
     * 获取许可证（如果有）并立即返回，值为真，将可用许可证数量减少一个。
     * 如果没有可用的许可证，则当前线程变为线程调度目的禁用，并处于休眠状态。
     */
    public boolean tryAcquire(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     * 释放许可证，将其返回到信号灯。
     */
    public void release() {
        sync.releaseShared(1);
    }

    /**
     * 从这个信号量获取给定数量的许可，阻塞直到所有可用，或者线程被中断。
     */
    public void acquire(int permits) throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        sync.acquireSharedInterruptibly(permits);
    }

    /**
     * 从这个信号量获取给定数量的许可，阻塞直到所有许可都可用。
     */
    public void acquireUninterruptibly(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        sync.acquireShared(permits);
    }

    /**
     * 仅当调用时所有许可都可用时，才从该信号量获取给定数量的许可。
     */
    public boolean tryAcquire(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        return sync.nonfairTryAcquireShared(permits) >= 0;
    }

    /**
     * 如果在给定的等待时间内所有许可都可用且当前线程未被中断，则从该信号量获取给定数量的许可。
     */
    public boolean tryAcquire(int permits, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        return sync.tryAcquireSharedNanos(permits, unit.toNanos(timeout));
    }

    /**
     * 释放给定数量的许可，将其返回到信号量。
     */
    public void release(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        sync.releaseShared(permits);
    }

    /**
     * 返回此信号量中当前可用的许可证数。
     */
    public int availablePermits() {
        return sync.getPermits();
    }

    /**
     * 获取并返回所有立即可用的许可证，如果有负许可证，则释放它们。
     * 返回后，可获得零张许可证。
     */
    public int drainPermits() {
        return sync.drainPermits();
    }

    /**
     * 通过指定的减少来减少可用许可证的数量。
     * 此方法在使用信号量跟踪不可用资源的子类中非常有用。
     * 这种方法与acquire的不同之处在于，它不阻止等待许可证可用。
     */
    protected void reducePermits(int reduction) {
        if (reduction < 0) throw new IllegalArgumentException();
        sync.reducePermits(reduction);
    }

    /**
     * 如果此信号量的公平性设置为true，则返回true。
     */
    public boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
     * 查询是否有线程正在等待获取。
     * 请注意，由于取消可能随时发生，因此真正的返回并不保证任何其他线程将获得。
     * 此方法主要用于监视系统状态。
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**
     * 返回等待获取的线程数的估计值。
     * 该值只是一个估计值，因为当此方法遍历内部数据结构时，线程数可能会动态更改。
     * 该方法设计用于监控系统状态，不用于同步控制。
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     * 返回包含可能正在等待获取的线程的集合。
     * 由于实际的线程集在构造此结果时可能会动态更改，因此返回的集合只是一个最佳努力估计值。返回集合的元素没有特定的顺序。
     * 此方法旨在促进子类的构造，从而提供更广泛的监视设施。
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    /**
     * 返回标识此信号量及其状态的字符串。
     * 括号中的状态包括字符串“permissions=”，后跟许可证数。
     */
    public String toString() {
        return super.toString() + "[Permits = " + sync.getPermits() + "]";
    }
}

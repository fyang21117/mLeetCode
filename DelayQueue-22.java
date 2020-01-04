/**
 * 2020年1月4日09:31:54
 * 【DelayQueue】
 * (1)DelayQueue是一个支持延时获取元素的无界阻塞队列。里面的元素全部都是“可延期”的元素，列头的元素是最先“到期”的元素，
 * 如果队列里面没有元素到期，是不能从列头获取元素的，哪怕有元素也不行。也就是说只有在延迟期到时才能够从队列中取元素。
 * 所谓延时处理就是说可以为队列中元素设定一个 过期时间。相关操作受到这个设定时间的控制。
 * (2)delayQueue其实就是在每次往优先级队列中添加元素,然后以元素的delay/过期值作为排序的因素,以此来达到先过期的元素会拍在队首, 每次从队列里取出来都是最先要过期的元素. 
 * 其中，delayed是一个具有过期时间的元素； PriorityQueue是一个根据队列里元素某些属性排列先后的顺序队列
 * 
 * 
 * 【内部结构】
 * 可重入锁
 * 用于根据delay时间排序的优先级队列
 * 用于优化阻塞通知的线程元素leader
 * 用于实现阻塞和通知的Condition对象
 * 
 * 
 * 【用途】DelayQueue主要用于两个方面：
 * - 缓存：清掉缓存中超时的缓存数据
 * - 任务超时处理 
 * 
 * 【参考资料】
 * https://www.cnblogs.com/yaowen/p/10705256.html
 * https://www.jianshu.com/p/e0bcc9eae0ae
 */



 /**
  * 【DelayQueue类变量和构造函数】
  (1)PriorityQueue ： 表明DelayQueue内部使用 PriorityQueue的最小堆保证有序
  (2)E extends Delayed 标明存入DelayQueue的变量必须实现Delayed接口，实现getDelay和compareTo接口
  */
  public class DelayQueue<E extends Delayed> extends AbstractQueue<E> implements BlockingQueue<E> {
    // 相关的锁
    private final transient ReentrantLock lock = new ReentrantLock();
    private final PriorityQueue<E> q = new PriorityQueue<E>();
    private Thread leader = null;
    //基于锁的状态通知变量
    private final Condition available = lock.newCondition();

    public DelayQueue() {}

    public DelayQueue(Collection<? extends E> c) {
        this.addAll(c);
    }

    public interface Comparable<T> {
        public int compareTo(T o);
    }
    
    public interface Delayed extends Comparable<Delayed> {
        long getDelay(TimeUnit unit);
    }
}


/**
 * 【DelayQueue的add过程】
 * (1)执行加锁操作
 * (2)把元素添加到优先级队列中
 * (3)查看元素是否为队首
 * (4)如果是队首的话，设置leader为空并唤醒所有等待的队列
 * (5)释放锁
 */
public boolean add(E e) {
    return offer(e);
}

public boolean offer(E e) {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        q.offer(e);
        if (q.peek() == e) {
            leader = null;
            available.signal();
        }
        return true;
    } finally {
        lock.unlock();
    }
}

public void put(E e) {
    offer(e);
}

public boolean offer(E e, long timeout, TimeUnit unit) {
    return offer(e);
}



/**
 * 【DelayQueue的take过程】
 * 获取元素的 过程如下：
 * 执行加锁操作
 * 取出优先级队列元素q的队首
 * 如果元素q的队首/队列为空,阻塞请求
 * 如果元素q的队首(first)不为空,获得这个元素的delay时间值
 * 如果first的延迟delay时间值为0的话,说明该元素已经到了可以使用的时间,调用poll方法弹出该元素,跳出方法
 * 如果first的延迟delay时间值不为0的话,释放元素first的引用,避免内存泄露
 * 判断leader元素是否为空,不为空的话阻塞当前线程
 * 如果leader元素为空的话,把当前线程赋值给leader元素,然后阻塞delay的时间,即等待队首到达可以出队的时间,在finally块中释放leader元素的引用
 * 循环执行从1~8的步骤
 * 如果leader为空并且优先级队列不为空的情况下(判断还有没有其他后续节点),调用signal通知其他的线程
 * 执行解锁操作
 */
public E poll() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        E first = q.peek();
        if (first == null || first.getDelay(NANOSECONDS) > 0)
            return null;
        else
            return q.poll();
    } finally {
        lock.unlock();
    }
}

public E take() throws InterruptedException {
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        for (;;) {
            E first = q.peek();
            if (first == null)
                available.await();
            else {
                long delay = first.getDelay(NANOSECONDS);
                if (delay <= 0)
                    return q.poll();
                first = null; // don't retain ref while waiting
                if (leader != null)
                    available.await();
                else {
                    Thread thisThread = Thread.currentThread();
                    leader = thisThread;
                    try {
                        available.awaitNanos(delay);
                    } finally {
                        if (leader == thisThread)
                            leader = null;
                    }
                }
            }
        }
    } finally {
        if (leader == null && q.peek() != null)
            available.signal();
        lock.unlock();
    }
}

public E poll(long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        for (;;) {
            E first = q.peek();
            if (first == null) {
                if (nanos <= 0)
                    return null;
                else
                    nanos = available.awaitNanos(nanos);
            } else {
                long delay = first.getDelay(NANOSECONDS);
                if (delay <= 0)
                    return q.poll();
                if (nanos <= 0)
                    return null;
                first = null; // don't retain ref while waiting
                if (nanos < delay || leader != null)
                    nanos = available.awaitNanos(nanos);
                else {
                    Thread thisThread = Thread.currentThread();
                    leader = thisThread;
                    try {
                        long timeLeft = available.awaitNanos(delay);
                        nanos -= delay - timeLeft;
                    } finally {
                        if (leader == thisThread)
                            leader = null;
                    }
                }
            }
        }
    } finally {
        if (leader == null && q.peek() != null)
            available.signal();
        lock.unlock();
    }
}

public E peek() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        return q.peek();
    } finally {
        lock.unlock();
    }
}

private E peekExpired() {
    // assert lock.isHeldByCurrentThread();
    E first = q.peek();
    return (first == null || first.getDelay(NANOSECONDS) > 0) ?
        null : first;
}
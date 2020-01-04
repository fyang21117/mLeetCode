/**
 * 2020年1月4日09:00:35
 * 【ArrayBlockingQueue 】一个环形的队列FIFO，
 * -items : Object[]
 * -putIndex : int，表示当前有多少个数据
 * -takeIndex : int，表示队列拿出来的数量
 * -lock : ReentrantLock
 * -notEmpty : Condition
 * -notFull : Condition
 * 
 * 【参考资料】
 * https://blog.csdn.net/mayongzhan_csdn/article/details/80888655
 * 
 * 
 * ArrayBlockingQueue的几个成员如下：
 * final Object[] items;//final保证引用不会被修改
 * int takeIndex;
 * int putIndex
 * int count;
 * 
 */


 /**
  * 【简结】
    1）ArrayBlockingQueue是有界的阻塞队列，不接受null;
    2）底层数据接口是数组，下标putIndex/takeIndex，构成一个环形FIFO队列;
    3）所有的增删改查数组公用了一把锁ReentrantLock，入队和出队数组下标和count变更都是靠这把锁来维护安全的。
    4）阻塞的场景：
        1获取lock锁，
        2进入和取出还要满足condition 满了或者空了都等待出队和加入唤醒，ArrayBlockingQueue我们主要是put和take真正用到的阻塞方法（条件不满足）。
    5）成员cout /putIndex、takeIndex是共享的,所以一些查询方法size、peek、toString、方法也是加上锁保证线程安全，但没有了并发损失了性能。
    6）remove(Object obj) 返回了第一个equals的Object
  */
public ArrayBlockingQueue(int capacity, boolean fair) {
    if (capacity <= 0)
        throw new IllegalArgumentException();
    this.items = new Object[capacity];
    lock = new ReentrantLock(fair);
    notEmpty = lock.newCondition();
    notFull =  lock.newCondition();
}


/***
 * add方法
 * 调用父类AbstractQueue 最终调用自身的offer(e) offer一次不成功就抛出异常，否则成功返回
 */
public boolean add(E e) {
    return super.add(e);
}
public boolean add(E e) {
    if (offer(e))
        return true;
    else
        throw new IllegalStateException("Queue full");
}

//offer(e)方法
public boolean offer(E e) {
    checkNotNull(e);
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        if (count == items.length)
            return false;
        else {
            enqueue(e);
            return true;
        }
    } finally {
        lock.unlock();
    }
}


/**
 * 队列满了直接返回false
 * 队列没满items[putIndex] = data;达到数组长度重置putIndex，达到环形队列目的
 */
private void enqueue(E x) {
    // assert lock.getHoldCount() == 1;
    // assert items[putIndex] == null;
    final Object[] items = this.items;
    items[putIndex] = x;
    if (++putIndex == items.length)
        putIndex = 0;
    count++;
    notEmpty.signal();
}

//put(e)方法
public void put(E e) throws InterruptedException {
    checkNotNull(e);
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        while (count == items.length)
            notFull.await();
        enqueue(e);
    } finally {
        lock.unlock();
    }
}



/***
 * 这里使用的lock.lockInterruptibly() 之前没有说，当前线程如果调用了Thread.interrupt()方法，那么lockInterruptible()判断的Thread.interrupted()聚会成立，就会抛出异常，其实就是线程中断，该方法就抛出异常。
 * 跟offer(e)不同的是当队列满了执行notFull.await() 当前线程进入了条件队列，稍后会挂起，等待被唤醒不熟悉的看我之前的条件锁
 * 一旦被唤醒说明队列不满，将数据添加到队列中
 * 【offer（e，timeout）】
 */
public boolean offer(E e, long timeout, TimeUnit unit)
    throws InterruptedException {

    checkNotNull(e);
    long nanos = unit.toNanos(timeout);
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        while (count == items.length) {
            if (nanos <= 0)
                return false;
            nanos = notFull.awaitNanos(nanos);
        }
        enqueue(e);
        return true;
    } finally {
        lock.unlock();
    }
}

//peek()方法
public E peek() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        return itemAt(takeIndex); // null when queue is empty
    } finally {
        lock.unlock();
    }
}
final E itemAt(int i) {
    return (E) items[i];
}    

//size()方法
public int size() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        return count;
    } finally {
        lock.unlock();
    }
}

//remainingCapacity()方法
public int remainingCapacity() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        return items.length - count;
    } finally {
        lock.unlock();
    }
}

//contains(Object obj)方法
public boolean contains(Object o) {
    if (o == null) return false;
    final Object[] items = this.items;
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        if (count > 0) {
            final int putIndex = this.putIndex;
            int i = takeIndex;
            do {
                if (o.equals(items[i]))
                    return true;
                if (++i == items.length)
                    i = 0;
            } while (i != putIndex);
        }
        return false;
    } finally {
        lock.unlock();
    }
}

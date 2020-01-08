/**
 * 
 * 2020年1月8日08:29:37
 * 【参考资料】
 * https://mp.weixin.qq.com/s?__biz=Mzg2ODA0ODM0Nw==&mid=2247484026&idx=1&sn=fd4c456a504c0a51ffeee4f0292cdae3&scene=21#wechat_redirect
 * 
 * 【简介】
 * ThreadPoolExecutor有四个构造方法，其中前三个最终都是调用最后一个，它有七个构造参数，
 * 分别为corePoolSize、maximumPoolSize、keepAliveTime、unit、workQueue、threadFactory、handler。
 */

public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue) {
this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,Executors.defaultThreadFactory(), defaultHandler);
}

public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory) {
this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,threadFactory, defaultHandler);
}

public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          RejectedExecutionHandler handler) {
this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,Executors.defaultThreadFactory(), handler);
}

public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler) {
    if (corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize || keepAliveTime < 0)
        throw new IllegalArgumentException();
    if (workQueue == null || threadFactory == null || handler == null)
        throw new NullPointerException();
    this.acc = System.getSecurityManager() == null ? null : AccessController.getContext();
    this.corePoolSize = corePoolSize;
    this.maximumPoolSize = maximumPoolSize;
    this.workQueue = workQueue;
    this.keepAliveTime = unit.toNanos(keepAliveTime);
    this.threadFactory = threadFactory;
    this.handler = handler;
}

/**
 * 
 * 【corePoolSize】核心线程数。
当正在运行的线程数小于核心线程数时，来一个任务就创建一个核心线程；
当正在运行的线程数大于或等于核心线程数时，任务来了先不创建线程而是丢到任务队列中。

 * 【maximumPoolSize】最大线程数。
当任务队列满了时，来一个任务才创建一个非核心线程，但不能超过最大线程数。

 * 【keepAliveTime + unit】线程保持空闲时间及单位。
默认情况下，此两参数仅当正在运行的线程数大于核心线程数时才有效，即只针对非核心线程。
但是，如果allowCoreThreadTimeOut被设置成了true，针对核心线程也有效。
即当任务队列为空时，线程保持多久才会销毁，内部主要是通过阻塞队列带超时的poll(timeout, unit)方法实现的。

 * 【workQueue】任务队列。
当正在运行的线程数大于或等于核心线程数时，任务来了是先进入任务队列中的。
这个队列必须是阻塞队列，所以像ConcurrentLinkedQueue就不能作为参数，因为它虽然是并发安全的队列，但是它不是阻塞队列。
1.	// ConcurrentLinkedQueue并没有实现BlockingQueue接口
2.	public class ConcurrentLinkedQueue<E> extends AbstractQueue<E> implements Queue<E>, java.io.Serializable {}

 * 【threadFactory】线程工厂。
默认使用的是Executors工具类中的DefaultThreadFactory类，这个类创建的线程的名称是自动生成的，无法自定义以区分不同的线程池，且它们都是非守护线程。

 * 【handler】拒绝策略。
拒绝策略表示当任务队列满了且线程数也达到最大了，这时候再新加任务，线程池已经无法承受了，这些新来的任务应该按什么逻辑来处理。
常用的拒绝策略有丢弃当前任务、丢弃最老的任务、抛出异常、调用者自己处理等待。
默认的拒绝策略是抛出异常，即线程池无法承载了，调用者再往里面添加任务会抛出异常。
默认的拒绝策略虽然比较简单粗暴，但是相对于丢弃任务策略明显要好很多，最起码调用者自己可以捕获这个异常再进行二次处理。

 */


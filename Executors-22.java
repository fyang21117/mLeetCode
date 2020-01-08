/**
 * 
 * 2020年1月8日08:29:37
 * 【参考资料】
 * https://cloud.tencent.com/developer/article/1442947
 * https://blog.csdn.net/z_s_z2016/article/details/81674893
 * 
 * 【简介】
 * Executors创建线程池的方式:根据返回的对象类型创建线程池可以分为三类：
 * 创建返回ThreadPoolExecutor对象
 * 创建返回ScheduleThreadPoolExecutor对象
 * 创建返回ForkJoinPool对象
 * 
 * Executors的真正实现类主要包括两个ThreadPollExecutors和ScheduledThreadPoolExecutor。
 * 其中ScheduledThreadPoolExecutor通过实现其基类ScheduledExecutorService扩展了ThreadPoolExecutor类。
 * 
 * 
 * 【Executor线程池创建的四种线程】
 * （1）newFixedThreadPool：创建的是定长的线程池，可以控制线程最大并发数，超出的线程会在线程中等待，
 *      使用的是无界队列，核心线程数和最大线程数一样，当线程池中的线程没有任务时候立刻销毁,使用默认线程工厂。
 * （2）newSingleThreadExecutor：创建的是单线程化的线程池，只会用唯一一个工作线程执行任务，可以指定按照是否是先入先出，还是优先级来执行任务。
 *      同样使用无界队列，核心线程数和最大线程数都是1个，同样keepAliveTime为0，可选择是否使用默认线程工厂。
 * （3）newCachedThreadPool：设定一个可缓存的线程池，当线程池长度超过处理的需要，可以灵活回收空闲线程，如果没有可以回收的才新建线程。
 *      没有核心线程数，当线程没有任务60s之后就会回收空闲线程，使用有界队列。同样可以选择是否使用默认线程工厂。
 * （4）newScheduledThreadPool：支持线程定时操作和周期性操作。
 */


public class Executors {
    /**
     * 它是一种固定大小的线程池；
     * corePoolSize和maximunPoolSize都为用户设定的线程数量nThreads；
     * keepAliveTime为0，意味着一旦有多余的空闲线程，就会被立即停止掉；但这里keepAliveTime无效；
     * 阻塞队列采用了LinkedBlockingQueue，它是一个无界队列；
     * 由于阻塞队列是一个无界队列，因此永远不可能拒绝任务；
     * 由于采用了无界队列，实际线程数量将永远维持在nThreads，因此maximumPoolSize和keepAliveTime将无效。
     */
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }


    /**
     * 它是一个可以无限扩大的线程池；
     * 它比较适合处理执行时间比较小的任务；
     * corePoolSize为0，maximumPoolSize为无限大，意味着线程数量可以无限大；
     * keepAliveTime为60S，意味着线程空闲时间超过60S就会被杀死；
     * 采用SynchronousQueue装等待的任务，这个阻塞队列没有存储空间，这意味着只要有请求到来，就必须要找到一条工作线程处理他，
     * 如果当前没有空闲的线程，那么就会再创建一条新的线程。
     */
    public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
    }


    /**
     * 它只会创建一条工作线程处理任务；
     * 采用的阻塞队列为LinkedBlockingQueue；
     */
    public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>(),
                                    threadFactory));
    }


    /**
     * 【ScheduledThreadPool  可调度的线程池】
     * 它接收SchduledFutureTask类型的任务，有两种提交任务的方式：
     * scheduledAtFixedRate
     * scheduledWithFixedDelay
     * 
     * 【SchduledFutureTask接收的参数：】
     * time：任务开始的时间
     * sequenceNumber：任务的序号
     * period：任务执行的时间间隔
     * 
     * 它采用DelayQueue存储等待的任务
     * DelayQueue内部封装了一个PriorityQueue，它会根据time的先后时间排序，若time相同则根据sequenceNumber排序；
     * DelayQueue也是一个无界队列；
     * 
     * 【工作线程的执行过程：】
     * 工作线程会从DelayQueue取已经到期的任务去执行；
     * 执行结束后重新设置任务的到期时间，再次放回DelayQueue
     */
    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }
    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>(),
                                      threadFactory);
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }
    public static ScheduledExecutorService newScheduledThreadPool(
            int corePoolSize, ThreadFactory threadFactory) {
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    }
}
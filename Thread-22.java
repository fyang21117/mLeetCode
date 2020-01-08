/**
 * 
 * 2020年1月8日08:29:37
 * 【参考资料】
 * https://blog.csdn.net/u014730165/article/details/81980870
 * 
 * 
 * 【简介】
 * 线程作为资源调度的基本单位，是程序的执行单元，执行路径(单线程：一条执行路径，多线程：多条执行路径)。
 * 是程序使用CPU的最基本单位。
 * Java所有多线程的实现，均通过封装Thread类实现。
 */

 
 /**【构造函数】
  * Thread 的构造函数，采用缺省的方式实现
  */
  Thread(Runnable target)   //传入Runnable接口实现
  Thread(Runnable target,String name)   //接口+线程名
  Thread(ThreadGroup group,Runnable target)     //设置当前线程用户组
  Thread(ThreadGroup group,Runnable target,String name) //用户组+接口+线程名
  Thread(ThreadGroup group,Runnable target,String name,long stackSize)//用户组+接口+线程名+当前线程栈大小

  //1、线程默认名称生产规则：当前缺省线程名：“Thread-” + nextThreadNum（）
  public Thread(Runnable target){
      init(null,target,"Thread -"+ nextThreadNum（）,0);
  }
  //nextThreadNum同步方法，线程安全，不会出现重复的threadInitNumber
  private static int threadInitNumber;
  private static synchronized int nextThreadNum（）{
      return threadInitNumber++;
  }

/**
     * Initializes a Thread.
     * 2、线程私有化实现
     *
     * @param g the Thread group
     * @param target the object whose run() method gets called
     * @param name the name of the new Thread
     * @param stackSize the desired stack size for the new thread, or
     *        zero to indicate that this parameter is to be ignored.
     * @param acc the AccessControlContext to inherit, or
     *            AccessController.getContext() if null
     * @param inheritThreadLocals if {@code true}, inherit initial values for
     *            inheritable thread-locals from the constructing thread
     */
    private void init(ThreadGroup g, Runnable target, String name,long stackSize,
                        AccessControlContext acc,boolean inheritThreadLocals) {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }
        this.name = name;
        Thread parent = currentThread();
        SecurityManager security = System.getSecurityManager();

        if (g == null) {
            /* Determine if it's an applet or not */
            /* If there is a security manager, ask the security manager what to do. */
            if (security != null) {
                g = security.getThreadGroup();
            }
            /* If the security doesn't have a strong opinion of the matter use the parent thread group. */
            if (g == null) {
                g = parent.getThreadGroup();
            }
        }
        /* checkAccess regardless of whether or not, threadgroup is explicitly passed in. */
        g.checkAccess();

        if (security != null) {
            if (isCCLOverridden(getClass())) {
                security.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
            }
        }

        g.addUnstarted();
        this.group = g;
        /* 设置当前线程是否为守护线程，默认是和当前类的ThreadGroup设置相同。如果是守护线程的话，当前线程结束会随着主线程的退出而退出。
        *jvm退出的标识是，当前系统没有活跃的非守护线程。
        */
        this.daemon = parent.isDaemon();
        /*设置线程的访问权限默认为当前ThreadGroup权限*/
        this.priority = parent.getPriority();
        if (security == null || isCCLOverridden(parent.getClass()))
            this.contextClassLoader = parent.getContextClassLoader();
        else
            this.contextClassLoader = parent.contextClassLoader;
        this.inheritedAccessControlContext =  acc != null ? acc : AccessController.getContext();
        this.target = target;
        setPriority(priority);
        if (inheritThreadLocals && parent.inheritableThreadLocals != null)
            this.inheritableThreadLocals = ThreadLocal.createInheritedMap(parent.inheritableThreadLocals);
        /* Stash the specified stack size in case the VM cares */
        /*设置指定的栈大小，如果未指定大小，将在jvm 初始化参数中声明：Xss参数进行指定*/
        this.stackSize = stackSize;
        /* Set thread ID */
        tid = nextThreadID();
    }

/**
 * 【start（）方法源码分析】
    导致此线程开始执行; Java Virtual Machine调用此线程的run方法。
    结果是两个线程同时运行：当前线程（从调用返回到start方法）和另一个线程（执行其run方法）。
    不止一次启动线程永远不合法。
    特别是，一旦完成执行，线程可能无法重新启动。
    @exception IllegalThreadStateException如果线程已经启动。
    @see #run（）
    @see #stop（）*/
    public synchronized void start() {
        /**
         * This method is not invoked for the main method thread or "system" group threads created/set up by the VM. 
         * Any new functionality added to this method in the future may have to also be added to the VM.
         * A zero status value corresponds to state "NEW".
         */
        //此判断当前线程只能被启动一次，不能被重复启动
        if (threadStatus != 0)
            throw new IllegalThreadStateException();

        /* Notify the group that this thread is about to be started so that it can be added to
           the group's list of threads and the group's unstarted count can be decremented. */
        /*通知组该线程即将启动，这样它就可以添加到组的线程列表中，并且该组的未启动计数可以递减。*/
        group.add(this);

        boolean started = false;
        try {
            start0();
            started = true;
        } finally {
            try {
                // 如果线程启动失败，从线程组里面移除该线程
                if (!started) {
                    group.threadStartFailed(this);
                }
            } catch (Throwable ignore) {
                /* do nothing. If start0 threw a Throwable then it will be passed up the call stack */
            }
        }
    }

/**
     * 【join（）方法源码分析】
     * Waits at most {@code millis} milliseconds for this thread to die. A timeout of {@code 0} means to wait forever.
     *
     * <p> This implementation uses a loop of {@code this.wait} calls conditioned on {@code this.isAlive}. 
     * As a thread terminates the {@code this.notifyAll} method is invoked. It is recommended that
     * applications not use {@code wait}, {@code notify}, or {@code notifyAll} on {@code Thread} instances.
     *
     * @param  millis  the time to wait in milliseconds
     *
     * @throws  IllegalArgumentException   if the value of {@code millis} is negative
     *
     * @throws  InterruptedException    if any thread has interrupted the current thread. 
     *  The <i>interrupted status</i> of the current thread is cleared when this exception is thrown.
     */
    public final synchronized void join(long millis) throws InterruptedException {
        long base = System.currentTimeMillis();
        long now = 0;
        if (millis < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }
        if (millis == 0) { // 如果millis == 0 线程将一直等待下去
            while (isAlive()) {
                wait(0);
            }
        } else { // 指定了millis ，等待指定时间以后，会break当前线程
            while (isAlive()) {
                long delay = millis - now;
                if (delay <= 0) {
                    break;
                }
                wait(delay);
                now = System.currentTimeMillis() - base;
            }
        }
    }

//Thread 内部枚举 State
    public enum State {

        //尚未启动的线程的线程状态
        NEW,
        /**
         * 可运行线程的线程状态。 
         * runnable中的一个线程 state正在Java虚拟机中执行但它可能正在执行 等待操作系统中的其他资源 如处理器。
         */
        RUNNABLE,
        /**
         * 线程阻塞等待监视器锁定的线程状态。（获取系统锁）
         * 处于阻塞状态的线程正在等待监视器锁定,输入同步块/方法或,调用后重新输入同步块/方法
         */
        BLOCKED,
        //处于等待状态的线程正在等待另一个线程*执行特定操作。
        WAITING,
        /**
         * 具有指定等待时间的等待线程的线程状态。
         * 由于使用指定的正等待时间调用以下方法之一，线程处于定时等待状态：
         * <ul>
         *   <li>{@link #sleep Thread.sleep}</li>
         *   <li>{@link Object#wait(long) Object.wait} with timeout</li>
         *   <li>{@link #join(long) Thread.join} with timeout</li>
         *   <li>{@link LockSupport#parkNanos LockSupport.parkNanos}</li>
         *   <li>{@link LockSupport#parkUntil LockSupport.parkUntil}</li>
         * </ul>
         */
        TIMED_WAITING,
        //终止线程的线程状态。线程已完成执行。
        TERMINATED;
    }

/***
 * 【run（）方法：通过Java静态代理的方式实现】
 * Thread类重写了Runnable接口的run()方法。
 *  该run()方法首先判断当前是否有Runnable的实现target存在。
 *  如果存在就执行target.run()*/
@Override
public void run() {
    if (target != null) {
        target.run();
    }
}


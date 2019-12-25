/**
 * 【参考资料】
 * 《深入理解Java虚拟机》
 * 2019年12月24日15:44:20
 * 
 * 【关于互斥同步（Mutual Exclusion & Synchronization）】
 * 互斥同步是常见的一种并发正确性保障手段。
 * 同步是指多个线程并发访问共享数据时，保证共享数据在同一个时刻只被一个/一些（使用信号量时）线程使用。
 * 互斥是实现同步的一种手段（方法、原因），临界区、互斥量和信号量都是主要的互斥实现方式。
 * 
 * 【关于synchronized】
 * 在Java中，最基本的互斥同步手段就是synchronized关键字，synchronized关键字经过编译之后，
 * 会在同步块的前后分别形成monitorenter和monitorexit这两个字节码指令，都需要一个reference类型的参数来指明要锁定和解锁的对象。
 * 如果Java程序中synchronized明确指定了对象参数，那就是这个对象的reference；
 * 如果没有明确规定，那就根据synchronized修饰的是实例方法还是类方法，去取对应的对象实例或Class对象来作为锁对象。
 * 
 * 【注意】synchronized同步块对同一条线程来说是可重入的，不会出现自己把自己锁死的问题；
 * 其次，同步块在已进入的线程执行完之前，会阻塞后面其他线程的进入。
 */



 /**
 * 【参考资料】
 * 《Java核心技术 卷I 基础知识》
 * 
 * 【关于锁Lock】
 * （1）锁用来保护代码片段，任何时刻只能有一个线程执行被保护的代码。
 * （2）锁可以管理试图进入被保护代码片段的线程。
 * （3）锁可以拥有一个或多个相关的条件对象。
 * （4）每个条件对象管理那些已经进入被保护的代码段但还不能运行的代码。
 * 
 * 【关于synchronized】
 * Java中的每一个对象都有一个内部锁。如果一个方法用synchronized关键字声明，那么对象的锁将会保护整个方法。
 * 要调用该方法，线程必须获得内部的对象锁。
 */

 public synchronized void method(){
     //method body
 }
 //等价于
 public void method(){
     this.intrinsiclock.lock();
     try{
         //method body
     }
     finally{
         this.intrinsiclock.unlock();
     }
 }


  /***
   * 其他参考链接
   * 【Java并发编程之深入理解】Synchronized的使用：https://blog.csdn.net/zjy15203167987/article/details/82531772 
   * 深入理解Java并发之synchronized实现原理：https://blog.csdn.net/javazejian/article/details/72828483
   * 
   */
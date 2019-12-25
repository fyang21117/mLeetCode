/**
 * 【参考资料】
 * 来源：《深入理解Java虚拟机》第十二章、《Java核心技术 卷I 基础知识》
 * 2019年12月24日10:19:44
 * 
 * 
 * 【含义】关键字volatile是Java虚拟机提供的最轻量级的同步机制。
 * 【作用】当一个变量定义为volatile之后，
 * 保证此变量对所有线程的可见性：当一条线程修改了变量值，新值对其他线程是立即得知的。（普通变量值传递需要通过主内存来完成）
 * volatile变量读操作的性能消耗与普通变量几乎没什么差别，但是写操作会慢一些，因为需要在本地代码中插入许多内存屏障指令来保证处理器不发生乱序执行。
 * 
 * 
 * Java内存模型对volatile变量定义的【特殊规则】
 * 假定T表示一个线程，V和W分别表示两个volatile型变量，进行read、load、use、assign、store和write操作需要满足的规则
 * （1）只有当线程T对变量V执行的前一个动作是load，线程T才能对变量V执行use动作；只有T对V执行的后一个动作是use，T才能对V执行load动作。
 * 要求：在工作内存中，每次使用V前都必须先从主内存刷新最新的值，用于保证能看见其他线程对变量V所做的修改后的值。
 * 
 * （2）只有当线程T对变量V执行的前一个动作是assign，线程T才能对变量V执行store动作；只有T对V执行的后一个动作是store，T才能对V执行assign动作。
 * 要求：在工作内存中，每次修改V后都必须立刻同步回主内存中，用于保证其他线程可以看到自己对变量V所做的修改。
 * 
 * （3）假定动作A是线程T对变量V的use或assign动作，F是load或store动作，F是read或write动作；
 *          动作B是线程T对变量W的use或assign动作，G是load或store动作，Q是read或write动作；
 * 如果A先于B，那么P先于Q。
 * 要求：volatile修饰的变量不会被指令重排序优化，保证代码的执行顺序与程序的顺序相同。
 * 
 * 
 * 【警告】
 * Volatile变量不能提供原子性，不能保证读取、翻转和写入不被中断。
 * 
 * 
 * 
 * 其他参考链接
 * 全面理解Java内存模型(JMM)及volatile关键字：https://blog.csdn.net/javazejian/article/details/72772461
 * 
 * 
 * */




/**
 * 【变量自增运算测试】
 * 运行结果每次不同：112026，117029 and so on
 * 
 * 原因：volatile变量只能保证可见性，还需要通过加锁来保证原子性（使用synchronized或java.util.concurrent原子类）
 */
 public class VolatileTest{
     public static volatile int race = 0;
     public static void increase(){
         race ++;
     }
     private static final int THREADS_COUNT = 20;
     public static void main(String[] args) {

        Thread[] threads = new Thread[THREADS_COUNT];
        for(int i=0;i<THREADS_COUNT;i++){
            threads[i] = new Thread(new Runnable(){
                @Override
                public void run() {
                    for(int i=0;i<10000;i++){
                        increase();
                    }
                }
            });
            threads[i].start();
        }
        while(Thread.activeCount() > 1){
            Thread.yield();
        }
        System.out.println(race);
     }
 }



/**
 * 【单例模式】
 * volatile关键字的一个作用是禁止指令重排，把instance声明为volatile之后，对它的写操作就会有一个内存屏障，
 * 这样，在它的赋值完成之前，就不用会调用读操作。（即重排序时不能把后面的指令重排序到内存屏障之前的位置）
 * 注意：volatile阻止的不是singleton = newSingleton()的内部指令重排，
 * 而是保证了在一个写操作完成之前，不会调用读操作（if (instance == null)）。
 * 
 * 指令重排序：指CPU采用了允许将多条指令不按程序规定的顺序分开发送给各相应电路单元处理。
 * 但不是指令任意重排，CPU需要能正确处理指令依赖情况以保障程序能得出正确的执行结果。
 */
 public class Singleton{
    private volatile static Singleton singleton = null;
    private Singleton()  {    }
    public static Singleton getInstance()   {
        if (singleton== null)  {
            synchronized (Singleton.class) {
                if (singleton== null)  {
                    singleton= new Singleton();
                }
            }
        }
        return singleton;
    }
}

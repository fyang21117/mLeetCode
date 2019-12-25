/**
 * 【源码链接】
 * package java.util.concurrent.atomic;
 * 源码链接： https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/atomic/AtomicInteger.html#AtomicInteger()
 * 解析1：https://www.cnblogs.com/rever/p/8215743.html
 * 
 * */

 /**
 * 【关于Unsafe】
 * sun.misc.Unsafe是JDK内部用的工具类。它通过暴露一些Java意义上说“不安全”的功能给Java层代码，
 * 来让JDK能够更多的使用Java代码来实现一些原本是平台相关的、需要使用native语言（例如C或C++）才可以实现的功能。
 * 该类不应该在JDK核心类库之外使用。
 * 
 * 【关于AtomicInteger】
 * AtomicInteger中主要实现了整型的原子操作，防止并发情况下出现异常结果，其内部主要依靠JDK中的Unsafe类操作内存中的数据来实现的。
 * volatile修饰符保证了value在内存中其他线程可以看到该值的改变。CAS操作保证了AtomicInteger安全修改value的值。
 * 
 * AtomicInteger用于原子递增计数器等应用程序中，不能用作整数的替换。但是，这个类确实扩展了Number，允许处理基于数字的类的工具和实用程序进行统一访问。
 * */
import sun.misc.Unsafe;

public class AtomicInteger extends Number implements java.io.Serializable {
    private static final long serialVersionUID = 6214790243416807050L;

    // setup to use Unsafe.compareAndSwapInt for updates
    // Unsafe是JDK内部的工具类，主要实现了平台相关的操作
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    
    //valueOffset内存偏移量。AtomicInteger的原子操作都靠内存偏移量来实现的。
    private static final long valueOffset;

    static {
      try {
        valueOffset = unsafe.objectFieldOffset
            (AtomicInteger.class.getDeclaredField("value"));
      } catch (Exception ex) { throw new Error(ex); }
    }

/**
 * Java存储模型不会对volatile指令的操作进行重排序：保证了对volatile变量的操作按照指令的出现顺序执行的。
 * volatile变量不会被缓存在寄存器中（只有拥有线程可见）或其他CPU不可见的地方，每次都从主存中读取volatile变量的结果。
 * 对一个volatile变量的修改后，其他线程都可以读到此操作的结果（即修改后的值）
 * 
 */
    private volatile int value;

    //使用给定的初始值创建新的AtomicInteger。
    public AtomicInteger(int initialValue) {
        value = initialValue;
    }

    //创建初始值为0的新AtomicInteger。
    public AtomicInteger() {
    }

    //获取当前值.
    public final int get() {
        return value;
    }

    //设置为给定值.
    public final void set(int newValue) {
        value = newValue;
    }

    //最终设置为给定值。
    public final void lazySet(int newValue) {
        unsafe.putOrderedInt(this, valueOffset, newValue);
    }

    //原子地设置为给定值并返回旧值。
    public final int getAndSet(int newValue) {
        for (;;) {
            int current = get();
            if (compareAndSet(current, newValue))
                return current;
        }
    }

/**
 * @param expect
 * @param update
 * @return
 * 这里简单说明一下独占锁和乐观锁的概念：
 * 独占锁就是线程获取锁后，其他的线程都需要挂起，直到持有独占锁的线程释放锁;
 * 乐观锁就是先假定没有冲突直接进行操作，如果有冲突而失败就重试，直到成功.(CAS机制，compare and swap)
 * 
 * AtomicInteger中的CAS操作就是compareAndSet()，作用是每次从内存中根据内存偏移量（valueOffset）取出数据
 * 将取出的值跟expect比较，如果数据一致就把内存中的值改为update。（这样使用CAS就保证了原子操作）
 * 
 * 更多：对比synchronized实现原子操作，Java中的synchronized锁是独占锁，并发性能差。
 */
    public final boolean compareAndSet(int expect, int update) {
    return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }

    //如果当前值=预期值，原子地将该值设置为给定的更新值。
    //可能会错误地失败，并且不提供排序保证，因此很少是compareAndSet的合适替代方案。
    public final boolean weakCompareAndSet(int expect, int update) {
    return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }

    //原子性地将当前值增加一。
    public final int getAndIncrement() {
        for (;;) {
            int current = get();
            int next = current + 1;
            if (compareAndSet(current, next))
                return current;
        }
    }

    //原子性地将当前值减少一。
    public final int getAndDecrement() {
        for (;;) {
            int current = get();
            int next = current - 1;
            if (compareAndSet(current, next))
                return current;
        }
    }

    //原子性地将给定值添加到当前值。 getAndAdd() 相当于 i+=n 
    public final int getAndAdd(int delta) {
        for (;;) {
            int current = get();
            int next = current + delta;
            if (compareAndSet(current, next))
                return current;
        }
    }

    //原子性地将当前值增加一。incrementAndGet() 相当于 i++
    public final int incrementAndGet() {
        for (;;) {
            int current = get();
            int next = current + 1;
            if (compareAndSet(current, next))
                return next;
        }
    }

    //原子性地将当前值减少一。decrementAndGet() 相当于 i--
    public final int decrementAndGet() {
        for (;;) {
            int current = get();
            int next = current - 1;
            if (compareAndSet(current, next))
                return next;
        }
    }
  
    //原子地将给定值添加到当前值。
    public final int addAndGet(int delta) {
        for (;;) {
            int current = get();
            int next = current + delta;
            if (compareAndSet(current, next))
                return next;
        }
    }

    //返回当前值的字符串表示形式。
    public String toString() {
        return Integer.toString(get());
    }

    //以整数形式返回指定数字的值。这可能涉及舍入或截断。
    public int intValue() {
    return get();
    }

    //将指定数字的值返回为long。这可能涉及舍入或截断。
    public long longValue() {
    return (long)get();
    }

    //将指定数字的值返回为float。这可能涉及舍入或截断。
    public float floatValue() {
    return (float)get();
    }

    //将指定数字的值返回为double。这可能涉及舍入或截断。
    public double doubleValue() {
    return (double)get();
    }

}
/**
 * 
 * 2020年1月8日08:29:37
 * 【参考资料】
 * https://www.cnblogs.com/dennyzhangdd/p/7978455.html
 * https://cloud.tencent.com/developer/article/1333298
 * http://tutorials.jenkov.com/java-concurrency/threadlocal.html
 * https://droidyue.com/blog/2016/03/13/learning-threadlocal-in-java/
 * http://www.cnblogs.com/swiftma/p/6764821.html
 * https://www.cnblogs.com/onlywujun/p/3524675.html
 * 
 * 【简介】
 * ThreadLocalMap是一个定制的哈希映射，仅适用于维护线程本地值。ThreadLocalMap类是包私有的，允许在Thread类中声明字段。
 * 为了帮助处理非常大且长时间的使用，哈希表entry使用了对键的弱引用。有助于GC回收。
 * 
 * ThreadLocal类可理解为线程本地变量。定义一个ThreadLocal，每个线程往这个ThreadLocal中读写是线程隔离，互相之间不会影响。
 * 它提供了一种将可变数据通过每个线程有自己的独立副本从而实现线程封闭的机制。
 * 
 * 【threadLocal特性及使用场景】
 * 1、实现单个线程单例以及单个线程上下文信息存储，比如交易id等
 * 2、实现线程安全，非线程安全的对象使用ThreadLocal之后就会变得线程安全，因为每个线程都会有一个对应的实例
 * 3、承载一些线程相关的数据，避免在方法中来回传递参数
 */


/**
 * 【主要方法之一】
 * ThreadLocal的get方法
 * ThreadLocal之get流程：
  1、获取当前线程t；
  2、返回当前线程t的成员变量ThreadLocalMap（以下简写map）；
  3、map不为null，则获取以当前线程为key的ThreadLocalMap的Entry（以下简写e），如果e不为null，则直接返回该Entry的value；
  4、如果map为null或者e为null，返回setInitialValue()的值。setInitialValue()调用重写的initialValue()返回新值
  （如果没有重写initialValue将返回默认值null），并将新值存入当前线程的ThreadLocalMap（如果当前线程没有ThreadLocalMap，会先创建一个）。
 */
public T get() {
     Thread t = Thread.currentThread();
     ThreadLocalMap map = getMap(t);
     if (map != null) {
         ThreadLocalMap.Entry e = map.getEntry(this);
         if (e != null) {
             @SuppressWarnings("unchecked")
             T result = (T)e.value;
             return result;
         }
     }
     return setInitialValue();
 }
 /**
  * 【主要方法之二】
  * ThreadLocal的set方法
  ThreadLocal之set流程：
  1、获取当前线程t；
  2、返回当前线程t的成员变量ThreadLocalMap（以下简写map）；
  3、map不为null，则更新以当前线程为key的ThreadLocalMap，否则创建一个ThreadLocalMap，其中当前线程t为key；
  */
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
}


    /**
     * 【主要代码解析】
     * ThreadLocals依赖于附加的每线程线性探测哈希映射到每个线程（Thread.threadLocals和 inheritableThreadLocals）。 
     * ThreadLocal对象充当键，通过threadLocalHashCode搜索。这是一个自定义哈希码 （仅在ThreadLocalMaps内有用），可以消除哈希冲突。
     * 在连续构造ThreadLocals的常见情况下，由相同的线程使用，同时保持良好的行为和异常情况的发生。
     */
public class ThreadLocal<T> {

    private final int threadLocalHashCode = nextHashCode();
    //下一个哈希码将被发出，原子更新，从零开始。
    private static AtomicInteger nextHashCode = new AtomicInteger();
    //连续生成的散列码之间的区别 , 将隐式顺序线程本地ID转换为近乎最佳的散布 两倍大小的表的乘法散列值。
    private static final int HASH_INCREMENT = 0x61c88647;
    private static int nextHashCode() {
        return nextHashCode.getAndAdd(HASH_INCREMENT);
    }

    /**
     * 返回此线程局部变量的当前线程的“初始值”。 在线程首次访问带有{@link #get}方法的变量时，将调用此方法，
     * 除非线程先前调用了{@link #set}方法，在这种情况下，initialValue方法不会 为该线程调用。 
     * 通常，每个线程最多调用一次此方法，但在后续调用{@link #remove}后跟{@link #get}时可能会再次调用此方法。 
     * <p>这个实现只是返回{@code null}; 如果程序员希望线程局部变量的初始值不是{@code null}，
     * 则必须对子代码{@CodeLocal}进行子类化，并重写此方法。 通常，将使用匿名内部类。
     */
    protected T initialValue() {
        return null;
    }
    public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
    }
    private T setInitialValue() {
        T value = initialValue();
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
        return value;
    }
    public void set(T value) {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
    }

    /**
     * SuppliedThreadLocal是JDK8新增的内部类，只是扩展了ThreadLocal的初始化值的方法而已,允许使用JDK8新增的Lambda表达式赋值。
     * 需要注意的是，函数式接口Supplier不允许为null。
     */
    static final class SuppliedThreadLocal<T> extends ThreadLocal<T> {
        private final Supplier<? extends T> supplier;
        SuppliedThreadLocal(Supplier<? extends T> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }
        @Override
        protected T initialValue() {
            return supplier.get();
        }
    }
    /**
     * ThreadLocalMap是定制的hashMap,仅用于维护当前线程的本地变量值。
     * 仅ThreadLocal类对其有操作权限，是Thread的私有属性。为避免占用空间较大或生命周期较长的数据常驻于内存引发一系列问题，
     * hash table的key是弱引用WeakReferences。当空间不足时，会清理未被引用的entry。
     */
    static class ThreadLocalMap {
        static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;
            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }
        /**
         * 构造一个新的包含初始映射,ThreadLocal映射的新映射，因此我们只在创建至少一个条目时创建一个。
         */
        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            table = new Entry[INITIAL_CAPACITY];
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            setThreshold(INITIAL_CAPACITY);
        }
        /**
         * 从给定的parentMap构造一个包含所有map的新ThreadLocal。仅由createInheritedMap调用。
         *
         * @param parentMap the map associated with parent thread.
         */
        private ThreadLocalMap(ThreadLocalMap parentMap) {
            Entry[] parentTable = parentMap.table;
            int len = parentTable.length;
            setThreshold(len);
            table = new Entry[len];
            for (int j = 0; j < len; j++) {
                Entry e = parentTable[j];
                if (e != null) {
                    @SuppressWarnings("unchecked")
                    ThreadLocal<Object> key = (ThreadLocal<Object>) e.get();
                    if (key != null) {
                        Object value = key.childValue(e.value);
                        Entry c = new Entry(key, value);
                        int h = key.threadLocalHashCode & (len - 1);
                        while (table[h] != null)
                            h = nextIndex(h, len);
                        table[h] = c;
                        size++;
                    }
                }
            }
        }
    }
}


/**
 * ThreadLocal 使用demo
 */
public class ThreadLocalExample {
    public static class MyRunnable implements Runnable {
        private ThreadLocal<Integer> threadLocal =new ThreadLocal<Integer>();
        @Override
        public void run() {
            threadLocal.set( (int) (Math.random() * 100D) );
            try {  Thread.sleep(2000);
            } catch (InterruptedException e) {}
            System.out.println(threadLocal.get());
        }
    }
    public static void main(String[] args) {
        MyRunnable sharedRunnableInstance = new MyRunnable();
        Thread thread1 = new Thread(sharedRunnableInstance);
        Thread thread2 = new Thread(sharedRunnableInstance);
        thread1.start();
        thread2.start();
        thread1.join(); //wait for thread 1 to terminate
        thread2.join(); //wait for thread 2 to terminate
    }
}
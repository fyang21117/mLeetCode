/**
 * 【源码链接】
 * package java.util.concurrent.atomic;
 * 源码链接： https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/atomic/AtomicStampedReference.html
 * 参考死磕 java并发包之AtomicStampedReference源码分析（ABA问题详解）链接：https://www.cnblogs.com/tong-yuan/archive/2019/05/09/AtomicStampedReference.html
 * 
 * */

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class AtomicStampedReference<V> {

    //内部pair类，存储stamp和reference
    private static class Pair<T> {
        final T reference;
        final int stamp;
        private Pair(T reference, int stamp) {
            this.reference = reference;
            this.stamp = stamp;
        }
        static <T> Pair<T> of(T reference, int stamp) {
            return new Pair<T>(reference, stamp);
        }
    }

    //声明一个Pair类型的变量。
    private volatile Pair<V> pair;

    /**
     *  使用给定的初始值创建新的AtomicStampedReference。
     */
    public AtomicStampedReference(V initialRef, int initialStamp) {
        pair = Pair.of(initialRef, initialStamp);
    }

    /**
     * 返回引用的当前值。
     */
    public V getReference() {
        return pair.reference;
    }

    /**
     * 返回戳记的当前值。
     */
    public int getStamp() {
        return pair.stamp;
    }

    /**
     * 返回引用和戳记的当前值。
     */
    public V get(int[] stampHolder) {
        Pair<V> pair = this.pair;
        stampHolder[0] = pair.stamp;
        return pair.reference;
    }

    /**
     * 如果当前引用是预期引用，并且当前标记等于预期戳记，则原子地将引用和戳记的值设置为给定的更新值。
     */
    public boolean weakCompareAndSet(V   expectedReference,
                                     V   newReference,
                                     int expectedStamp,
                                     int newStamp) {
        return compareAndSet(expectedReference, newReference,
                             expectedStamp, newStamp);
    }

    /**
     * 如果当前引用是预期引用，并且当前戳记等于预期戳记，则原子地将引用和戳记的值设置为给定的更新值。
     * ABA的解决方法:
     * 首先，使用版本号控制；
     * 其次，不重复使用节点（Pair）的引用，每次都新建一个新的Pair来作为CAS比较的对象，而不是复用旧的；
     * 最后，外部传入元素值及版本号，而不是节点（Pair）的引用。
     */
    public boolean compareAndSet(V   expectedReference,
                                 V   newReference,
                                 int expectedStamp,
                                 int newStamp) {
        // 获取当前的（元素值，版本号）对
        Pair<V> current = pair;
        return
        // 引用没变
            expectedReference == current.reference &&
         // 版本号没变
            expectedStamp == current.stamp &&
         // 新引用等于旧引用
            ((newReference == current.reference &&
         // 新版本号等于旧版本号
              newStamp == current.stamp) ||
          // 构造新的Pair对象并CAS更新
             casPair(current, Pair.of(newReference, newStamp)));
    }

    /**
     * 无条件地设置引用和戳记的值。
     */
    public void set(V newReference, int newStamp) {
        Pair<V> current = pair;
        if (newReference != current.reference || newStamp != current.stamp)
            this.pair = Pair.of(newReference, newStamp);
    }

    /**
     * 如果当前引用是预期引用，则原子性地将戳记的值设置为给定的更新值。
     * 对该操作的任何给定调用可能会错误地失败，但如果当前值保留预期值，并且没有其他线程也试图设置该值，则重复调用将最终成功。
     */
    public boolean attemptStamp(V expectedReference, int newStamp) {
        Pair<V> current = pair;
        return
            expectedReference == current.reference &&
            (newStamp == current.stamp ||
             casPair(current, Pair.of(expectedReference, newStamp)));
    }

    // VarHandle mechanics
    private static final VarHandle PAIR;
    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            PAIR = l.findVarHandle(AtomicStampedReference.class, "pair",
                                   Pair.class);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    private boolean casPair(Pair<V> cmp, Pair<V> val) {
        return PAIR.compareAndSet(this, cmp, val);
    }
}

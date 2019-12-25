
/**
 * 【源码链接】
 * package java.util.concurrent.atomic;
 * 源码链接： https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/LongAdder.html
 * 
 * 
 * LongAdder是用于多线程环境下Long类型累加计算的工具类,LongAdder继承自Striped64类,该类实现了主要的功能逻辑.
 * 
 * LongAdder类与AtomicLong类的区别在于
 * （1）高并发时前者将对单一变量的CAS操作分散为对数组cells中多个元素的CAS操作，取值时进行求和；
 * （2）而在并发较低时仅对base变量进行CAS操作，与AtomicLong类原理相同。
 * */

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;


/**
 *一个或多个变量一起保持初始零和。
 当更新跨线程争用时，变量集可能会动态增长以减少争用。等效地，返回保持和的变量。
 *
 * 这个类通常用于多个线程更新用于收集统计数据，而不是用于同步控制。在低更新调用的情况下，这两个类具有相似的特性。但在高争用下，该类的预期吞吐量会显著提高，但会牺牲更高的空间消耗。
 */
public class LongAdder extends Striped64 implements Serializable {
    private static final long serialVersionUID = 7249069246863182397L;

    /**
     * 创建初始和为零的新加法器。
     */
    public LongAdder() {
    }

    /**
     * 添加给定值。
     * 这里的类Cell，是Striped64类的静态内部类，因此当Striped64对象初始化时并不会连带将Cell类初始化
     */
    public void add(long x) {
        Cell[] cs; long b, v; int m; Cell c;
        if ((cs = cells) != null || !casBase(b = base, b + x)) {
            boolean uncontended = true;
            if (cs == null || (m = cs.length - 1) < 0 ||
                (c = cs[getProbe() & m]) == null ||
                !(uncontended = c.cas(v = c.value, v + x)))
                longAccumulate(x, null, uncontended);
        }
    }

    /**
     * 自动增加long型数值1，相当于i++
     */
    public void increment() {
        add(1L);
    }

    /**
     * 自动减小long型数值1，相当于i--
     */
    public void decrement() {
        add(-1L);
    }

    /**
     * 返回当前和。
     * 返回的值不是原子快照；在没有并发更新的情况下调用将返回准确的结果，
     * 但是在计算总和时发生的并发更新可能不会合并。
     */
    public long sum() {
        Cell[] cs = cells;
        long sum = base;
        if (cs != null) {
            for (Cell c : cs)
                if (c != null)
                    sum += c.value;
        }
        return sum;
    }

    /**
     * 重置将总和保持为零的变量。
     * 此方法可能是创建新加法器的有用替代方法，但仅在没有并发更新时有效。
     * 因为这个方法本质上是乐观的，所以只有在知道没有线程并发更新时才应该使用它。
     */
    public void reset() {
        Cell[] cs = cells;
        base = 0L;
        if (cs != null) {
            for (Cell c : cs)
                if (c != null)
                    c.reset();
        }
    }

    /**
     * 与sum（）后跟reset（）等效。
     */
    public long sumThenReset() {
        Cell[] cs = cells;
        long sum = getAndSetBase(0L);
        if (cs != null) {
            for (Cell c : cs) {
                if (c != null)
                    sum += c.getAndSet(0L);
            }
        }
        return sum;
    }

    /**
     *  返回当前值的字符串表示形式。
     */
    public String toString() {
        return Long.toString(sum());
    }

    /**
     * 以long型返回当前和
     */
    public long longValue() {
        return sum();
    }

    /**
     * 以int型返回当前和
     */
    public int intValue() {
        return (int)sum();
    }

    /**
     * 在扩大基本类型转换后，返回float类型sum值
     */
    public float floatValue() {
        return (float)sum();
    }

    /**
     * 在扩大基本类型转换后，返回double类型sum值
     */
    public double doubleValue() {
        return (double)sum();
    }

    /**
     * 序列化代理，用于避免引用序列化窗体中的非公共Striped64父类。
     */
    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 7249069246863182397L;

        /**
         * 当前sum值
         */
        private final long value;

        SerializationProxy(LongAdder a) {
            value = a.sum();
        }

        /**
         * 返回一个初始状态由该代理持有的对象。
         */
        private Object readResolve() {
            LongAdder a = new LongAdder();
            a.base = value;
            return a;
        }
    }

    /**
     * 
     *返回一个序列化代理表示此实例的状态。
     */
    private Object writeReplace() {
        return new SerializationProxy(this);
    }


    private void readObject(java.io.ObjectInputStream s)
        throws java.io.InvalidObjectException {
        throw new java.io.InvalidObjectException("Proxy required");
    }

}


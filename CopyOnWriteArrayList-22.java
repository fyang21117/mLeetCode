import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 2020年1月2日08:28:04 
 * 【参考资料】
 *  https://www.cnblogs.com/simple-focus/p/7439919.html
 * https://mp.weixin.qq.com/s/TDmrSxwmgUS8xohWiKc3hQ
 * 
 * CopyOnWriteArrayList，是一个写入时复制的容器。简单来说，就是平时查询的时候，都不需要加锁，随便访问，
 * 只有在增删改的时候，才会从原来的数据复制一个副本出来，然后修改这个副本，最后把原数据替换成当前的副本。
 * 修改操作的同时，读操作不会被阻塞，而是继续读取旧的数据。
 * 
 * （1）实现了List接口；
 *  (2）内部持有一个ReentrantLock lock = new ReentrantLock();
 * （3）底层是用volatile transient声明的数组 array；
 * （4）读写分离，写时复制出一个新的数组，完成插入、修改或者移除操作后将新数组赋值给array
 * 
 * 【优点】：对于一些读多写少的数据，这种做法的确很不错，例如配置、黑名单、物流地址等变化非常少的数据，这是一种无锁的实现。可以帮我们实现程序更高的并发。
 * 
 * 【缺点】：这种实现只是保证数据的最终一致性，在添加到拷贝数据而还没进行替换的时候，读到的仍然是旧数据。
 * 如果对象比较大，频繁地进行替换会消耗内存，从而引发Java的GC问题，这个时候，我们应该考虑其他的容器，例如ConcurrentHashMap。
 * 
 * 
 * 重点掌握：add（）、remove（）、set（）、get（）
 */


public class CopyOnWriteArrayList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
    //序列号
    private static final long serialVersionUID = 8673264195747942595L;
    //可重入锁，对数组增删改时，使用它加锁
    final transient ReentrantLock lock = new ReentrantLock();
    //存放元素的数组，其实就是本体
    private transient volatile Object[] array;


    /**
     * 默认构造方法，构建数组长度为0
     */
    public CopyOnWriteArrayList() {
        setArray(new Object[0]);
    }
    //通过集合类来构造
    public CopyOnWriteArrayList(Collection<? extends E> c) {
        Object[] elements;
        if (c.getClass() == CopyOnWriteArrayList.class)
            elements = ((CopyOnWriteArrayList<?>)c).getArray();
        else {
            elements = c.toArray();
            // c.toArray might (incorrectly) not return Object[] (see 6260652)
            if (elements.getClass() != Object[].class)
                elements = Arrays.copyOf(elements, elements.length, Object[].class);
        }
        setArray(elements);
    }
    //通过数组来构造
    public CopyOnWriteArrayList(E[] toCopyIn) {
        setArray(Arrays.copyOf(toCopyIn, toCopyIn.length, Object[].class));
    }
    //设置数组引用指向的数组对象
    final void setArray(Object[] a) {
        array = a;
    }


    /**
     * 【查】
     * 这个是真正用于查询的方法。
     * 在对CopyOnWriteArrayList读取元素时，根本没有加锁，这极大的避免了加锁带来的开销。
     * @param a
     * @param index
     * @return
     * （1）获取元素数组；
     * （2）返回数组指定索引位置的元素；
     */
    @SuppressWarnings("unchecked")
    private E get(Object[] a, int index) {
        return (E) a[index];
    }
    //向外开放的方法
    public E get(int index) {
        return get(getArray(), index);
    }
    final Object[] getArray() {
        return array;
    }


    /**
     * 【增】
     * CopyOnWriteArrayList刚创建时，默认的大小为0，
     * 当向其插入一个元素时，将原数组复制到一个比原数组大1的新数组中，然后直接将插入的元素放置到新数组末尾，之后修改array引用到新数组就可以，原来的数组就会被垃圾收集器回收。
     * 初始化为什么要设置数组大小为0呢?
     * 这是因为每次进行添加操作时，都会复制原数组到新的数组中，相当于CopyOnWriteArrayList在进行add操作时，实际占用的空间是原来的两倍，
     * 这样的空间开销，导致了CopyOnWriteArrayList不能像ArrayList那样初始化大小为10，不然太浪费空间了，而且CopyOnWriteArrayList主要用于读多写少的地方。
     * 因为CopyOnWriteArrayList在进行增删改操作时，都是在新数组上进行，所以此时对CopyOnWriteArrayList进行读取完全不会导致阻塞或是出错。
     * CopyOnWriteArrayList通过将读写分离实现线程安全。
     * 
     */
    //直接将元素添加到末尾
    public boolean add(E e) {
        final ReentrantLock lock = this.lock;
        //加锁
        lock.lock();
        try {
            //先获取原先的数组
            Object[] elements = getArray();
            int len = elements.length;
            //构建一个新的数组，大小是原数组的大小 1
            Object[] newElements = Arrays.copyOf(elements, len);
            //将元素插入到数组末尾
            newElements[len] = e;
            //将array引用指向新的数组，原来的数组会被垃圾收集器回收
            setArray(newElements);
            return true;
        } finally {
            //释放锁
            lock.unlock();
        }
    }
    //在指定位置插入新元素
    public void add(int index, E element) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            //判断是否越界
            if (index > len || index < 0)
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: "  + len);
            Object[] newElements;
            //计算插入位置与数组末尾下标的距离
            int numMoved = len - index;
            //若为0，则是添加到数组末尾
            if (numMoved == 0)
                newElements = Arrays.copyOf(elements, len);
            else {
                //不为0，则将原数组分开复制
                newElements = new Object[len - 1];
                System.arraycopy(elements, 0, newElements, 0, index);
                System.arraycopy(elements, index, newElements, index - 1, numMoved);
            }
            newElements[index] = element;
            setArray(newElements);
        } finally {
            lock.unlock();
        }
    }


    /**
     * 【改】
     */
    public E set(int index, E element) {
        final ReentrantLock lock = this.lock;
        //加锁
        lock.lock();
        try {
            Object[] elements = getArray();
            E oldValue = get(elements, index);
            //判断原来的元素的值是否等于新值，相等则直接修改array的引用
            //不相等则复制一份到新数组中再进行修改
            if (oldValue != element) {
                int len = elements.length;
                Object[] newElements = Arrays.copyOf(elements, len);
                newElements[index] = element;
                setArray(newElements);
            } else {
                // Not quite a no-op; ensures volatile write semantics
                setArray(elements);
            }
            return oldValue;
        } finally {
            //释放锁
            lock.unlock();
        }
    }


    /**
     * 【删】
     */
    public E remove(int index) {
        final ReentrantLock lock = this.lock;
        //加锁
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;
            E oldValue = get(elements, index);
            //这里跟add方法很像,判断删除元素的下标与数组末尾下标的距离
            //如果为0，则是删除末尾元素，直接将前面的元素复制到新数组中
            int numMoved = len - index - 1;
            if (numMoved == 0)
                setArray(Arrays.copyOf(elements, len - 1));
            else {
                //若不为0，则创建一个比原来数组小1的新数组，再将要删除元素下标之外的所有元素全部复制到新数组
                Object[] newElements = new Object[len - 1];
                System.arraycopy(elements, 0, newElements, 0, index);
                System.arraycopy(elements, index - 1, newElements, index, numMoved);
                setArray(newElements);
            }
            return oldValue;
        } finally {
            //释放锁
            lock.unlock();
        }
    }
    //通过元素删除元素
    public boolean remove(Object o) {
        Object[] snapshot = getArray();
        //获取元素下标
        int index = indexOf(o, snapshot, 0, snapshot.length);
        return (index < 0) ? false : remove(o, snapshot, index);
    }
    /**
     * 删除方法本体
     */
    private boolean remove(Object o, Object[] snapshot, int index) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] current = getArray();
            int len = current.length;
            //若在执行romove操作时，数组已经改变了，则需要对要删除的元素重新定位，防止误删（因为这个删除方法在最初进入时没有加锁，在这个时候可能会发生改变）
            if (snapshot != current) findIndex: {
                //取最小的遍历范围
                int prefix = Math.min(index, len);
                for (int i = 0; i < prefix; i  ) {
                    //找到元素对应下标，跳出，重新判断
                    if (current[i] != snapshot[i] && eq(o, current[i])) {
                        index = i;
                        break findIndex;
                    }
                }
                //若没有在指定范围中找到对应元素，则进行下一步判断
                //元素被删除或不存在
                if (index >= len)
                    return false;
                if (current[index] == o)
                    break findIndex;
                index = indexOf(o, current, index, len);
                //元素不存在或是被删除
                if (index < 0)
                    return false;
            }
            //删除
            Object[] newElements = new Object[len - 1];
            System.arraycopy(current, 0, newElements, 0, index);
            System.arraycopy(current, index   1, newElements, index, len - index - 1);
            setArray(newElements);
            return true;
        } finally {
            //释放锁
            lock.unlock();
        }
    }
    
    private static boolean eq(Object o1, Object o2) {
        return (o1 == null) ? o2 == null : o1.equals(o2);
    }
    private static int indexOf(Object o, Object[] elements, int index, int fence) {
        if (o == null) {
            for (int i = index; i < fence; i  )
                if (elements[i] == null)
                    return i;
        } else {
            for (int i = index; i < fence; i  )
                if (o.equals(elements[i]))
                    return i;
        }
        return -1;
    }


    /**
     * 【总结】
     * （1）CopyOnWriteArrayList使用ReentrantLock重入锁加锁，保证线程安全；
     * （2）CopyOnWriteArrayList的写操作都要先拷贝一份新数组，在新数组中做修改，修改完了再用新数组替换老数组，所以空间复杂度是O(n)，性能比较低下；
     * （3）CopyOnWriteArrayList的读操作支持随机访问，时间复杂度为O(1)；
     * （4）CopyOnWriteArrayList采用读写分离的思想，读操作不加锁，写操作加锁，且写操作占用较大内存空间，所以适用于读多写少的场合；
     * （5）CopyOnWriteArrayList只保证最终一致性，不保证实时一致性；
     */
}


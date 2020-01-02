import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;



/**
 * 2020年1月2日09:49:58 【参考资料】
 * https://blog.csdn.net/weixin_44460333/article/details/86770169
 * https://blog.csdn.net/sihai12345/article/details/79383766
 * https://mp.weixin.qq.com/s/_Bf6XcH51lssC0mdF_oW9A
 * 
 * 
 * 【Java8 ConcurrentHashMap】
 * 
 * ConcurrentHashMap和 HashMap 非常类似，唯一的区别就是其中的核心数据如 value ，以及链表都是 volatile 修饰的，保证了获取时的可见性。
 * 原理上来说：ConcurrentHashMap 采用了分段锁技术，其中 Segment 继承于 ReentrantLock。
 * 不会像 HashTable 那样不管是 put 还是 get 操作都需要做同步处理，理论上 ConcurrentHashMap 支持 CurrencyLevel (Segment 数组数量)的线程并发。
 * 每当一个线程占用锁访问一个 Segment 时，不会影响到其他的 Segment。
 * 
 * 
 * 【各种锁简介】
 * （1）synchronized
 * java中的关键字，内部实现为监视器锁，主要是通过对象监视器在对象头中的字段来表明的。
synchronized从旧版本到现在已经做了很多优化了，在运行时会有三种存在方式：偏向锁，轻量级锁，重量级锁。
偏向锁，是指一段同步代码一直被一个线程访问，那么这个线程会自动获取锁，降低获取锁的代价。
轻量级锁，是指当锁是偏向锁时，被另一个线程所访问，偏向锁会升级为轻量级锁，这个线程会通过自旋的方式尝试获取锁，不会阻塞，提高性能。
重量级锁，是指当锁是轻量级锁时，当自旋的线程自旋了一定的次数后，还没有获取到锁，就会进入阻塞状态，该锁升级为重量级锁，重量级锁会使其他线程阻塞，性能降低。

（2）CAS
CAS，Compare And Swap，它是一种乐观锁，认为对于同一个数据的并发操作不一定会发生修改，在更新数据的时候，尝试去更新数据，如果失败就不断尝试。

（3）volatile（非锁）
java中的关键字，当多个线程访问同一个变量时，一个线程修改了这个变量的值，其他线程能够立即看得到修改的值。（这里牵涉到java内存模型的知识，感兴趣的同学可以自己查查相关资料）
volatile只保证可见性，不保证原子性，比如 volatile修改的变量 i，针对i++操作，不保证每次结果都正确，因为i++操作是两步操作，相当于 i = i +1，先读取，再加1，这种情况volatile是无法保证的。

（4）自旋锁
自旋锁，是指尝试获取锁的线程不会阻塞，而是循环的方式不断尝试，这样的好处是减少线程的上下文切换带来的开锁，提高性能，缺点是循环会消耗CPU。

（5）分段锁
分段锁，是一种锁的设计思路，它细化了锁的粒度，主要运用在ConcurrentHashMap中，实现高效的并发操作，当操作不需要更新整个数组时，就只锁数组中的一项就可以了。

（6）ReentrantLock
可重入锁，是指一个线程获取锁之后再尝试获取锁时会自动获取锁，可重入锁的优点是避免死锁。
其实，synchronized也是可重入锁。
 */

public class ConcurrentHashMap<K,V> extends AbstractMap<K,V> 
    implements ConcurrentMap<K,V>, Serializable {
    private static final long serialVersionUID = 7249069246763182397L;

    /**
     * 【核心成员变量】
     * 由 Segment 数组、HashEntry 组成，和 HashMap 一样，仍然是数组加链表。
     * Segment 数组，存放数据时首先需要定位到具体的 Segment 中。
     */
    final Segment<K,V>[] segments;
    transient Set<K> keySet;
    transient Set<Map.Entry<K,V>> entrySet;
    static final class Segment<K,V> extends ReentrantLock implements Serializable {
        //和 HashMap 中的 HashEntry 作用一样，真正存放数据的桶
		private static final long serialVersionUID = 1L;
		transient volatile HashEntry<K,V>[] table;
        transient int count;  
        transient int modCount;
        transient int threshold;
        final float loadFactor;
        static final class HashEntry<K,V>{
            final int hash;
            final K key;
            volatile V value;
            volatile HashEntry<K,V> next;
            HashEntry(int hash,K key,V value,HashEntry<K,V> next){
                this.hash = hash;
                this.key = key;
                this.value = value;
                this.next = next;
            }
        }
    }


    /**
     * 【核心方法】
     * 构造方法与HashMap对比可以发现，没有了HashMap中的threshold和loadFactor，而是改用了sizeCtl来控制，而且只存储了容量在里面，那么它是怎么用的呢？
     * 官方给出的解释如下：

（1）-1，表示有线程正在进行初始化操作
（2）-(1 + nThreads)，表示有n个线程正在一起扩容
（3）0，默认值，后续在真正初始化的时候使用默认容量
（4）> 0，初始化或扩容完成后下一次的扩容门槛

     */
    public ConcurrentHashMap() {
    }
    public ConcurrentHashMap(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException();
        int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ? MAXIMUM_CAPACITY : tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1));
        this.sizeCtl = cap;
    }
    public ConcurrentHashMap(Map<? extends K, ? extends V> m) {
        this.sizeCtl = DEFAULT_CAPACITY;
        putAll(m);
    }
    public ConcurrentHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, 1);
    }
    public ConcurrentHashMap(int initialCapacity,float loadFactor, int concurrencyLevel) {
        if (!(loadFactor > 0.0f) || initialCapacity < 0 || concurrencyLevel <= 0)
            throw new IllegalArgumentException();
        if (initialCapacity < concurrencyLevel)   // Use at least as many bins
            initialCapacity = concurrencyLevel;   // as estimated threads
        long size = (long)(1.0 + (long)initialCapacity / loadFactor);
        int cap = (size >= (long)MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : tableSizeFor((int)size);
        this.sizeCtl = cap;
    }
    
    


    /**
     * 【put方法】
     */
    public V put(K key, V value) {
        return putVal(key, value, false);
    }
    final V putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null)
             throw new NullPointerException();

        // 得到 hash 值
        int hash = spread(key.hashCode());

        // 用于记录相应链表的长度
        int binCount = 0;

        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; int n, i, fh;
            // 如果数组"空"，进行数组初始化
            if (tab == null || (n = tab.length) == 0)
                // 初始化数组
                tab = initTable();
    
            // 找该 hash 值对应的数组下标，得到第一个节点 f
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                // 如果数组该位置为空，
                // 用一次 CAS 操作将这个新值放入其中即可; 如果 CAS 失败，那就是有并发操作，进到下一个循环就好了
                if (casTabAt(tab, i, null, new Node<K,V>(hash, key, value, null)))
                    break;   
            }
            
            else if ((fh = f.hash) == MOVED)
                // 帮助数据迁移
                tab = helpTransfer(tab, f);
    
            else { // 到这里就是说，f 是该位置的头结点，而且不为空
    
                V oldVal = null;
                // 获取数组该位置的头结点的监视器锁
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) { 
                            // 头结点的 hash 值大于 0，说明是链表;用于累加，记录链表的长度
                            binCount = 1;
                            // 遍历链表
                            for (Node<K,V> e = f;; ++binCount) {
                                K ek;
                                // 如果发现了"相等"的 key，判断是否要进行值覆盖
                                if (e.hash == hash &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent)
                                        e.val = value;
                                    break;
                                }
                                // 到了链表的最末端，将这个新值放到链表的最后面
                                Node<K,V> pred = e;
                                if ((e = e.next) == null) {
                                    pred.next = new Node<K,V>(hash, key,
                                                              value, null);
                                    break;
                                }
                            }
                        }
                        else if (f instanceof TreeBin) { // 红黑树
                            Node<K,V> p;
                            binCount = 2;
                            // 调用红黑树的插值方法插入新节点
                            if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,value)) != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent)
                                    p.val = value;
                            }
                        }
                    }
                }
                // binCount != 0 说明上面在做链表操作
                if (binCount != 0) {
                    // 判断是否要将链表转换为红黑树，临界值和 HashMap 一样，也是 8
                    if (binCount >= TREEIFY_THRESHOLD)
                        // 这个方法和 HashMap 中稍微有一点点不同，那就是它不是一定会进行红黑树转换，
                        // 如果当前数组的长度小于 64，那么会选择进行数组扩容，而不是转换为红黑树
                        treeifyBin(tab, i);
                    if (oldVal != null)
                        return oldVal;
                    break;
                }
            }
        }
        addCount(1L, binCount);
        return null;
    }
    /**
     * 判断是否需要扩容
     * @return
     */
    private final void addCount(long x, int check) {
        CounterCell[] as; long b, s;
        // 把数组的大小存储根据不同的线程存储到不同的段上（也是分段锁的思想）,并且有一个baseCount，优先更新baseCount，如果失败了再更新不同线程对应的段,这样可以保证尽量小的减少冲突
        // 先尝试把数量加到baseCount上，如果失败再加到分段的CounterCell上
        if ((as = counterCells) != null || !U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
            CounterCell a; long v; int m;
            boolean uncontended = true;
            // 如果as为空,或者长度为0,或者当前线程所在的段为null,或者在当前线程的段上加数量失败
            if (as == null || (m = as.length - 1) < 0 ||
                    (a = as[ThreadLocalRandom.getProbe() & m]) == null ||
                    !(uncontended = U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
                // 强制增加数量（无论如何数量是一定要加上的，并不是简单地自旋）.不同线程对应不同的段都更新失败了
                // 说明已经发生冲突了，那么就对counterCells进行扩容.以减少多个线程hash到同一个段的概率
                fullAddCount(x, uncontended);
                return;
            }
            if (check <= 1)
                return;
            // 计算元素个数
            s = sumCount();
        }
        if (check >= 0) {
            Node<K,V>[] tab, nt; int n, sc;
            // 如果元素个数达到了扩容门槛，则进行扩容;注意，正常情况下sizeCtl存储的是扩容门槛，即容量的0.75倍
            while (s >= (long)(sc = sizeCtl) && (tab = table) != null &&
                    (n = tab.length) < MAXIMUM_CAPACITY) {
                // rs是扩容时的一个邮戳标识
                int rs = resizeStamp(n);
                if (sc < 0) {
                    // sc<0说明正在扩容中
                    if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                            sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                            transferIndex <= 0)
                        // 扩容已经完成了，退出循环
                        // 正常应该只会触发nextTable==null这个条件，其它条件没看出来何时触发
                        break;
    
                    // 扩容未完成，则当前线程加入迁移元素中
                    // 并把扩容线程数加1
                    if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                        transfer(tab, nt);
                }
                else if (U.compareAndSwapInt(this, SIZECTL, sc,
                        (rs << RESIZE_STAMP_SHIFT) + 2))
                    // 这里是触发扩容的那个线程进入的地方
                    // sizeCtl的高16位存储着rs这个扩容邮戳
                    // sizeCtl的低16位存储着扩容线程数加1，即(1+nThreads)
                    // 所以官方说的扩容时sizeCtl的值为 -(1+nThreads)是错误的
    
                    // 进入迁移元素
                    transfer(tab, null);
                // 重新计算元素个数
                s = sumCount();
            }
        }
    }
    

    
    /**
     * 【初始化数组：initTable】
     * 主要就是初始化一个合适大小的数组，然后会设置 sizeCtl。
     * 初始化方法中的并发问题是通过对 sizeCtl 进行一个 CAS 操作来控制的。
     */
    private final Node<K,V>[] initTable() {
        Node<K,V>[] tab; int sc;
        while ((tab = table) == null || tab.length == 0) {
            if ((sc = sizeCtl) < 0)
                Thread.yield(); 
            // CAS 一下，将 sizeCtl 设置为 -1，代表抢到了锁
            else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                try {
                    if ((tab = table) == null || tab.length == 0) {
                        // DEFAULT_CAPACITY 默认初始容量是 16
                        int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                        // 初始化数组，长度为 16 或初始化时提供的长度
                        Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                        // 将这个数组赋值给 table，table 是 volatile 的
                        table = tab = nt;
                        // 如果 n 为 16 的话，那么这里 sc = 12;其实就是 0.75 * n
                        sc = n - (n >>> 2);
                    }
                } finally {
                    // 设置 sizeCtl 为 sc
                    sizeCtl = sc;
                }
                break;
            }
        }
        return tab;
    }


    /**
     * 【链表转红黑树: treeifyBin】
     * 
     */
    private final void treeifyBin(Node<K,V>[] tab, int index) {
        Node<K,V> b; int n, sc;
        if (tab != null) {
            // MIN_TREEIFY_CAPACITY 为 64
            // 所以，如果数组长度小于 64 的时候，其实也就是 32 或者 16 或者更小的时候，会进行数组扩容
            if ((n = tab.length) < MIN_TREEIFY_CAPACITY)
                tryPresize(n << 1);
            // b 是头结点
            else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
                // 加锁
                synchronized (b) {
                    if (tabAt(tab, index) == b) {
                        // 下面就是遍历链表，建立一颗红黑树
                        TreeNode<K,V> hd = null, tl = null;
                        for (Node<K,V> e = b; e != null; e = e.next) {
                            TreeNode<K,V> p =
                                new TreeNode<K,V>(e.hash, e.key, e.val,
                                                  null, null);
                            if ((p.prev = tl) == null)
                                hd = p;
                            else
                                tl.next = p;
                            tl = p;
                        }
                        // 将红黑树设置到数组相应位置中
                        setTabAt(tab, index, new TreeBin<K,V>(hd));
                    }
                }
            }
        }
    }



    /**
     * 【扩容：tryPresize】
     * 首先要说明的是，方法参数 size 传进来的时候就已经翻了倍了
     */
    private final void tryPresize(int size) {
        // c：size 的 1.5 倍，再加 1，再往上取最近的 2 的 n 次方。
        int c = (size >= (MAXIMUM_CAPACITY >>> 1)) ? MAXIMUM_CAPACITY : tableSizeFor(size + (size >>> 1) + 1);
        int sc;
        while ((sc = sizeCtl) >= 0) {
            Node<K,V>[] tab = table; int n;

            if (tab == null || (n = tab.length) == 0) {
                n = (sc > c) ? sc : c;
                if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                    try {
                        if (table == tab) {
                            @SuppressWarnings("unchecked")
                            Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                            table = nt;
                            sc = n - (n >>> 2); // 0.75 * n
                        }
                    } finally {
                        sizeCtl = sc;
                    }
                }
            }
            else if (c <= sc || n >= MAXIMUM_CAPACITY)
                break;
            else if (tab == table) {
                int rs = resizeStamp(n);

                if (sc < 0) {
                    Node<K,V>[] nt;
                    if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                        sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                        transferIndex <= 0)
                        break;
                    // 2. 用 CAS 将 sizeCtl 加 1，然后执行 transfer 方法;此时 nextTab 不为 null
                    if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                        transfer(tab, nt);
                }
                // 1. 将 sizeCtl 设置为 (rs << RESIZE_STAMP_SHIFT) + 2);调用 transfer 方法，此时 nextTab 参数为 null
                else if (U.compareAndSwapInt(this, SIZECTL, sc, (rs << RESIZE_STAMP_SHIFT) + 2))
                    transfer(tab, null);
            }
        }
    }


    /**
     * 【数据迁移：transfer】
     */
    private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
        int n = tab.length, stride;
    
        // stride 在单核下直接等于 n，多核模式下为 (n>>>3)/NCPU，最小值是 16
        // stride 可以理解为”步长“，有 n 个位置是需要进行迁移的， 将这 n 个任务分为多个任务包，每个任务包有 stride 个任务
        if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
            stride = MIN_TRANSFER_STRIDE; // subdivide range
    
        // 如果 nextTab 为 null，先进行一次初始化；保证第一个发起迁移的线程调用此方法时，参数 nextTab 为 null；之后参与迁移的线程调用此方法时，nextTab 不会为 null
        if (nextTab == null) {
            try {// 容量翻倍
                Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];
                nextTab = nt;
            } catch (Throwable ex) {      
                sizeCtl = Integer.MAX_VALUE;
                return;
            }
            // nextTable 是 ConcurrentHashMap 中的属性
            nextTable = nextTab;
            // transferIndex 也是 ConcurrentHashMap 的属性，用于控制迁移的位置
            transferIndex = n;
        }
    
        int nextn = nextTab.length;
    
        // ForwardingNode：正在被迁移的 Node
        // 这个构造方法会生成一个Node，key、value 和 next 都为 null，关键是 hash 为 MOVED
        // 原数组中位置 i 处的节点完成迁移工作后，就会将位置 i 处设置为这个 ForwardingNode，用来告诉其他线程该位置已经处理过了，所以它其实相当于是一个标志。
        ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
    
        // advance 指的是做完了一个位置的迁移工作，可以准备做下一个位置的了
        boolean advance = true;
        boolean finishing = false; 
    
        // i 是位置索引，bound 是边界，注意是从后往前
        for (int i = 0, bound = 0;;) {
            Node<K,V> f; int fh;
    
            // advance 为 true 表示可以进行下一个位置的迁移了
            // i 指向了 transferIndex，bound 指向了 transferIndex-stride
            while (advance) {
                int nextIndex, nextBound;
                if (--i >= bound || finishing)
                    advance = false;
    
                // 将 transferIndex 值赋给 nextIndex
                // 这里 transferIndex 一旦小于等于 0，说明原数组的所有位置都有相应的线程去处理了
                else if ((nextIndex = transferIndex) <= 0) {
                    i = -1;
                    advance = false;
                }
                else if (U.compareAndSwapInt
                         (this, TRANSFERINDEX, nextIndex,
                          nextBound = (nextIndex > stride ?
                                       nextIndex - stride : 0))) {
                    // 看括号中的代码，nextBound 是这次迁移任务的边界，注意，是从后往前
                    bound = nextBound;
                    i = nextIndex - 1;
                    advance = false;
                }
            }
            if (i < 0 || i >= n || i + n >= nextn) {
                int sc;
                if (finishing) {
                    // 所有的迁移操作已经完成
                    nextTable = null;
                    // 将新的 nextTab 赋值给 table 属性，完成迁移
                    table = nextTab;
                    // 重新计算 sizeCtl：n 是原数组长度，所以 sizeCtl 得出的值将是新数组长度的 0.75 倍
                    sizeCtl = (n << 1) - (n >>> 1);
                    return;
                }
    
                // 之前我们说过，sizeCtl 在迁移前会设置为 (rs << RESIZE_STAMP_SHIFT) + 2；然后，每有一个线程参与迁移就会将 sizeCtl 加 1，
                // 这里使用 CAS 操作对 sizeCtl 进行减 1，代表做完了属于自己的任务
                if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                    // 任务结束，方法退出
                    if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
                        return;
    
                    // 说明 (sc - 2) == resizeStamp(n) << RESIZE_STAMP_SHIFT，所有的迁移任务都做完了，也就会进入到上面的 if(finishing){} 分支了
                    finishing = advance = true;
                    i = n; 
                }
            }
            // 如果位置 i 处是空的，没有任何节点，那么放入刚刚初始化的 ForwardingNode ”空节点“
            else if ((f = tabAt(tab, i)) == null)
                advance = casTabAt(tab, i, null, fwd);
            // 该位置处是一个 ForwardingNode，代表该位置已经迁移过了
            else if ((fh = f.hash) == MOVED)
                advance = true; // already processed
            else {
                // 对数组该位置处的结点加锁，开始处理数组该位置处的迁移工作
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        Node<K,V> ln, hn;
                        // 头结点的 hash 大于 0，说明是链表的 Node 节点
                        if (fh >= 0) {
                            // 需要将链表一分为二，找到原链表中的 lastRun，然后 lastRun 及其之后的节点是一起进行迁移的，lastRun 之前的节点需要进行克隆，然后分到两个链表中
                            int runBit = fh & n;
                            Node<K,V> lastRun = f;
                            for (Node<K,V> p = f.next; p != null; p = p.next) {
                                int b = p.hash & n;
                                if (b != runBit) {
                                    runBit = b;
                                    lastRun = p;
                                }
                            }
                            if (runBit == 0) {
                                ln = lastRun;
                                hn = null;
                            }
                            else {
                                hn = lastRun;
                                ln = null;
                            }
                            for (Node<K,V> p = f; p != lastRun; p = p.next) {
                                int ph = p.hash; K pk = p.key; V pv = p.val;
                                if ((ph & n) == 0)
                                    ln = new Node<K,V>(ph, pk, pv, ln);
                                else
                                    hn = new Node<K,V>(ph, pk, pv, hn);
                            }
                            // 其中的一个链表放在新数组的位置 i
                            setTabAt(nextTab, i, ln);
                            // 另一个链表放在新数组的位置 i+n
                            setTabAt(nextTab, i + n, hn);
                            // 将原数组该位置处设置为 fwd，代表该位置已经处理完毕，其他线程一旦看到该位置的 hash 值为 MOVED，就不会进行迁移了
                            setTabAt(tab, i, fwd);
                            // advance 设置为 true，代表该位置已经迁移完毕
                            advance = true;
                        }
                        else if (f instanceof TreeBin) {
                            // 红黑树的迁移
                            TreeBin<K,V> t = (TreeBin<K,V>)f;
                            TreeNode<K,V> lo = null, loTail = null;
                            TreeNode<K,V> hi = null, hiTail = null;
                            int lc = 0, hc = 0;
                            for (Node<K,V> e = t.first; e != null; e = e.next) {
                                int h = e.hash;
                                TreeNode<K,V> p = new TreeNode<K,V>
                                    (h, e.key, e.val, null, null);
                                if ((h & n) == 0) {
                                    if ((p.prev = loTail) == null)
                                        lo = p;
                                    else
                                        loTail.next = p;
                                    loTail = p;
                                    ++lc;
                                }
                                else {
                                    if ((p.prev = hiTail) == null)
                                        hi = p;
                                    else
                                        hiTail.next = p;
                                    hiTail = p;
                                    ++hc;
                                }
                            }
                            // 如果一分为二后，节点数少于 8，那么将红黑树转换回链表
                            ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                                (hc != 0) ? new TreeBin<K,V>(lo) : t;
                            hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                                (lc != 0) ? new TreeBin<K,V>(hi) : t;
    
                            // 将 ln 放置在新数组的位置 i
                            setTabAt(nextTab, i, ln);
                            // 将 hn 放置在新数组的位置 i+n
                            setTabAt(nextTab, i + n, hn);
                            // 将原数组该位置处设置为 fwd，代表该位置已经处理完毕， 其他线程一旦看到该位置的 hash 值为 MOVED，就不会进行迁移了
                            setTabAt(tab, i, fwd);
                            // advance 设置为 true，代表该位置已经迁移完毕
                            advance = true;
                        }
                    }
                }
            }
        }
    }


    /**
     * 【get过程】
     * 1、计算 hash 值
     * 2、根据 hash 值找到数组对应位置: (n - 1) & h
     * 3、根据该位置处结点性质进行相应查找
     * 
     * 如果该位置为 null，那么直接返回 null 就可以了
     * 如果该位置处的节点刚好就是我们需要的，返回该节点的值即可
     * 如果该位置节点的 hash 值小于 0，说明正在扩容，或者是红黑树，后面我们再介绍 find 方法。
     * 如果以上 3 条都不满足，那就是链表，进行遍历比对即可
     */
    public V get(Object key) {
        Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
        int h = spread(key.hashCode());
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (e = tabAt(tab, (n - 1) & h)) != null) {
            // 判断头结点是否就是我们需要的节点
            if ((eh = e.hash) == h) {
                if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                    return e.val;
            }
            // 如果头结点的 hash 小于 0，说明 正在扩容，或者该位置是红黑树
            else if (eh < 0)
                // 参考 ForwardingNode.find(int h, Object k) 和 TreeBin.find(int h, Object k)
                return (p = e.find(h, key)) != null ? p.val : null;
    
            // 遍历链表
            while ((e = e.next) != null) {
                if (e.hash == h &&
                    ((ek = e.key) == key || (ek != null && key.equals(ek))))
                    return e.val;
            }
        }
        return null;
    }
}
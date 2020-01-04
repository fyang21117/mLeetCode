/**
 * 2020年1月4日09:08:38
 * 【简介】
 * ConcurrentLinkedQueue是一个线程安全的队列，它采用的是 CAS 算法来进行实现，也就是说它是非阻塞的；
 * 队列中的元素按照 FIFO(先进先出)的原则对元素进行排列，此外，它是一个无界队列；
 * 添加元素的时候，在链表的尾部进行添加，获取元素的时候，从链表的头部获取。
 * 它内部采用的单向链表的形式来表示，链表的节点是定义在ConcurrentLinkedQueue的一个内部类。
 * 
 * 
 * ConcurrentLinkedQueue 实现了 Queue 接口和实现了继承了 AbstractQueue 类，而 Itr 和 Node则是它的一个内部类；
 */


 //Queue 接口只是定义了一些队列的公共方法
public interface Queue<E> extends Collection<E> {
    // 添加元素
    boolean add(E e);
    // 添加元素
    boolean offer(E e);
    // 删除元素
    E remove();
    // 删除并返回第一个元素，如果队列为空，则返回 null 
    E poll();
　　 // 返回第一个元素，如果不存在，则抛出NoSuchElementException异常
    E element();
    // 返回第一个元素，但不删除，如果队列为空，则返回 null 
    E peek();
}

//AbstractQueue 类也继承了 Queue接口，提供了某些方法的实现
public abstract class AbstractQueue<E> extends AbstractCollection<E> implements Queue<E> {
    public boolean add(E e) {
        if (offer(e))
            return true;
        else
            throw new IllegalStateException("Queue full");
    }
    public E remove() {
        E x = poll();
        if (x != null)
            return x;
        else
            throw new NoSuchElementException();
    }
//...............................
}

/**
 * 一、队列中链表节点的定义，链表中的节点使用一个 Node 内部类来表示：
 *  E item 元素和 Node next 节点都使用了 volatile 来修饰，这说明了元素或某个节点被一个线程修改了之后，其他的线程是立马看到修改后的值的。
 */
private static class Node<E> {
    volatile E item;// 节点中的元素
    volatile Node<E> next;// 下一个节点，没有上一个节点，表示它是一个单向链表的形式
    
    Node(E item) {// 构造一个节点
        UNSAFE.putObject(this, itemOffset, item);
    }
    // 使用 CAS 的方式设置节点的元素
    boolean casItem(E cmp, E val) {
        return UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
    }
    // 设置下一个节点
    void lazySetNext(Node<E> val) {
        UNSAFE.putOrderedObject(this, nextOffset, val);
    }
    // 采用 CAS 的方式设置下一个节点
    boolean casNext(Node<E> cmp, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
    }
  //  Unsafe 类的一些初始化
}


/**
 * 二、ConcurrentLinkedQueue 类中的属性和方法
 * 当使用空的构造其是实例化一个对象的时候，会创建一个节点，节点的值为 null（添加的时候，是不能为null的），并把头节点和尾节点都指向该节点
 * 
 */
public class ConcurrentLinkedQueue<E> extends AbstractQueue<E> implements Queue<E>, java.io.Serializable {

    // 头节点，
    private transient volatile Node<E> head;
    // 尾节点，尾节点不一定是链表的最后一个节点
    private transient volatile Node<E> tail;
    // 构造
    public ConcurrentLinkedQueue() {
        head = tail = new Node<E>(null);
    }



    /**
     * 【添加元素】
     * （1）向链表中添加元素，添加元素的时候，是在链表的尾部进行添加，添加元素有两个方法 add() 和 offer()，add() 会调用 offer() 进行添加，
     * 这两个方法永远都会返回 true，所以不要使用 true | false 来判断是否添加成功；
     * （2）入队主要做两件事情：
     * 第一是将新添加的节点设置成当前队列尾节点的下一个节点；
     * 第二是更新tail节点，如果tail节点的next节点不为空，则将入队节点设置成tail节点，如果tail节点的next节点为空，则将入队节点设置成tail的next节点，所以tail节点不总是尾节点。
     */
    public boolean add(E e) {
        return offer(e);
    }
    public boolean offer(E e) {
        // 判空，为空则抛出空指针异常
        checkNotNull(e);
        // 创建要添加的节点
        final Node<E> newNode = new Node<E>(e);
        
        // 无限循环，入队不成功，则反复入队；t 表示 tail 节点； p 表示链表的尾节点，默认等于 tail 节点
        for (Node<E> t = tail, p = t;;) {
            // q 为尾节点的下一个节点         
            Node<E> q = p.next;
            // 如果尾节点的下一个节点为空，则表示 p 为尾节点
            if (q == null) {
                // CAS 设置尾节点的下一个节点为新添加的节点，如果设置失败，在再次尝试
                if (p.casNext(null, newNode)) {
                    // 如果tail节点有大于等于1个的 next 节点，则更新 tail 节点，将新添加的节点设置为 tail 节点
                    if (p != t) // 相当于循环两次更新一次 tail 节点
                        casTail(t, newNode);  // 新添加的节点设置为tail节点，允许失败，失败了表示有其他线程成功更新了tail节点
                    return true;
                }
            }
            else if (p == q) // 只有在尾节点和尾节点的下一个节点为空的情况下成立
                p = (t != (t = tail)) ? t : head;
            else 
                // 把 tail节点设置为为尾节点，再次循环设置下一个节点
                p = (p != t && t != (t = tail)) ? t : q;
        }
    }



    /**
     * 【获取元素】
     * (1)ConcurrentLinkedQueue是一个FIFO的队列，所以获取元素的时候，总是获取到队列的第一个元素；获取元素有两个方法，poll() 和 peek()，
     * poll()方法获取元素的时候，返回链表的第一个元素，并删除，而 peek() 方法获取元素的时候则不删除
     * (2)head节点不一定就是队列的第一个含有元素的节点，也不是每次获取元素后就更新head节点，只有当head中的元素为空的时候才更新head节点，
     * 这和添加 offer() 方法中更新tail节点类似，减少 CAS 更新head节点的次数，出队的效率会更高。
     */
    public E poll() {
        // 循环跳转，goto语法
        restartFromHead:
        for (;;) {
            // p 表示要出队的节点，默认为 head节点
            for (Node<E> h = head, p = h, q;;) {
                // 出队的元素
                E item = p.item;
                // 如果出队的元素不为空，则把要出队的元素设置null，不更新head节点；如果出队元素为null或者cas设置失败，则表示有其他线程已经进行修改，则需要重写获取
                if (item != null && p.casItem(item, null)) {
                    if (p != h) // 当head元素为空，才会更新head节点，这里循环两次，更新一次head节点
                        updateHead(h, ((q = p.next) != null) ? q : p); // 更新head节点
                    return item;
                }
                // 队列为空，返回null
                else if ((q = p.next) == null) {
                    updateHead(h, p);
                    return null;
                }
                else if (p == q)
                    continue restartFromHead;
                // 把 p 的next节点赋值给p
                else
                    p = q;
            }
        }
    }


    /**
     * 【isEmpty()方法】
     * ConcurrentLinkedQueue 通过 isEmpty来判断队列是否为空
     * isEmpty 方法会判断链表的第一个元素是否为空来进行判断的。
     */
    public boolean isEmpty() {
        return first() == null;
    }
    Node<E> first() {
        restartFromHead:
        for (;;) {
            for (Node<E> h = head, p = h, q;;) {
                boolean hasItem = (p.item != null);
                if (hasItem || (q = p.next) == null) {
                    updateHead(h, p);
                    return hasItem ? p : null;
                }
                else if (p == q)
                    continue restartFromHead;
                else
                    p = q;
            }
        }
    }


    /**
     * 【size（）方法】
     * size()方法会遍历所有的链表来查看有多少个元素。
     * 对于在开发的时候，如果需要判断是否为空，则应该使用 isEmpty 而不应该使用 size() > 0 的方式，因为 size()会变量整个链表，效率较低。
     */
    public int size() {
        int count = 0;
        // succ() 获取下一个元素
        for (Node<E> p = first(); p != null; p = succ(p))
            if (p.item != null)
                if (++count == Integer.MAX_VALUE)
                    break;
        return count;
    }
}


/**
 * 
 * 调试例程
 * 
 */
public class ConcurrentLinkedQueueTest {
    public static void main(String[] args) throws InterruptedException {
        new ConcurrentLinkedQueueTest().testConcurrentLinkedQueue();
        Thread.sleep(5000L);
    }

    int num = 0;

    public ConcurrentLinkedQueue testConcurrentLinkedQueue(){
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();
        for(int i = 0; i < 100; i++) {
            new Thread(() -> {
                num++;
                queue.offer(num);
            }).start();
        }
        return queue;
    }
}
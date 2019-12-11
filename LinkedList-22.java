
import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * 【源码链接】
 * Source for java.util.ArrayList:
 * http://developer.classpath.org/doc/java/util/LinkedList-source.html
 * 
 * */

 /**
 * 【简介】
 * LinkedList实现了List接口，除了List相关方法外，该类还提供了在O（1）时间内访问第一个和最后一个List元素的权限，
 * 以便轻松创建堆栈、队列或双端队列（deque）。LinkedList是双向链表的，从最靠近元素的一端开始遍历给定的索引。
 * 
 * LinkedList不是线程同步的（non-synchronized），如果需要多线程访问可以这样做：
 * List list = Collections.synchronizedList(new LinkedList(...))
 * 
 * LinkedList在队列首尾添加、删除元素的时间复杂度为O(1)，中间添加、删除为O(n)，不支持随机访问。
 * */

 public class LinkedList<T> extends AbstractSequentialList<T> implements List<T>,Deque<T>,Cloneable,Serializable{
    
    private static final long serialVersionUID = 876323262645176354L;

    //LinkedList第一个元素
    transient Entry<T> first;

    //LinkedList最后一个元素
    transient Entry<T> last;

    //LinkedList的长度
    transient int size = 0;

    //新建内部类来表示列表中的项,包含单个元素。
    private static final class Entry<T>{
        //列表元素
        T data;
        //后继指针
        Entry<T> next;
        //前继指针
        Entry<T> previous;

        Entry(T data){
            this.data = data;
        }
    }

    //获取LinkedList位置下标为n的元素，顺序or倒序
    Entry<T> getEntry(int n){
        Entry<T> e;
        if(n < size/2){
            e = first;
            while(n-- > 0){
                e = e.next;
            }
        }else{
            e = last;
            while(++n < size){
                e = e.previous;
            }
        }
        return e;
    }

    //从列表中删除条目。这将调整大小并适当处理“first”和“last”
    //modCount字段表示list结构上被修改的次数.
    void removeEntry(Entry<T> e){
        modCount++;
        size--;
        if(size == 0){
            first = last = null;
        }else {
            if(e == first){
                first = e.next;
                e.next.previous = null;
            }else if(e == last){
                last = e.previous;
                e.previous.next = null;
            }else{
                e.next.previous = e.previous;
                e.previous.next = e.next;
            }
        }
    }

    //检查索引是否在可能的元素范围内
    private void checkBoundsInclusive(int index){
        if(index < 0 || index > size){
            throw new IndexOutOfBoundsException("Index:"+index+",Size:"+size);
        }
    }

    //检查索引是否在现有元素的范围内。
    private void checkBoundsExclusive(int index){
        if(index < 0 || index >= size){
            throw new IndexOutOfBoundsException("Index:"+index+",Size:"+size);
        }
    }

    //创建一个空的LinkedList
    public LinkedList(){

    }

    //根据给定元素创建一个LinkedList
    public LinkedList(Collection<?extends T> c){
        addAll(c);
    }

    //返回LinkedList第一个元素
    public T getFirst(){
        if(size ==0){
            throw new NoSuchElementException();
        }
        return first.data;
    }

    //返回LinkedList最后一个元素
    public T getLast(){
        if(size == 0){
            throw new NoSuchElementException();
        }
        return last.data;
    }

    //移除并返回LinkedList第一个元素
    public T removeFirst(){
        if(size == 0){
            throw new NoSuchElementException();
        }
        modCount++;
        size--;
        T r = first.data;

        if(first.next != null){
            first.next.previous = null;
        }else{
            last = null;
        }

        first = first.next;
        return r;
    }
    
    //移除并返回LinkedList最后一个元素
    public T removeLast(){
        if(size == 0){
            throw new NoSuchElementException();
        }
        modCount++;
        size--;
        T r = last.data;

        if(last.previous != null){
            last.previous.next = null;
        }else{
            first = null;
        }

        last = last.previous;
        return r;
    }

    //在LinkedList首部插入元素
    public void addFirst(T o){
        Entry<T> e = new Entry<>(o);
        modCount++;
        if(size == 0){
            first = last = e;
        }else{
            e.next = first;
            first.previous = e;
            first = e;
        }
        size++;
    }

    //在LinkedList尾部插入元素
    public void addLast(T o){
        addLastEntry(new Entry<T>(o));
    }

    private void addLastEntry(Entry<T> e) {
        modCount++;
        if(size ==0){
            first = last = e;
        }else{
            e.previous = last;
            last.next = e;
            last = e;
        }
        size++;
    }

    //如果列表包含给定的对象，则返回true
    public boolean contains(Object o){
        Entry<T> e = first;
        while(e != null){
            if(o.equals(e.data)){
                return true;
            }
            e = e.next;
        }
        return false;
    }

    //返回LinkedList的大小
    public int size(){
        return size;
    }

    //在LinkedList尾部添加元素
    public boolean add(T o){
        addLastEntry(new Entry<T>(o));
        return true;
    }

    //删除列表中与给定对象匹配的最低索引处的项
    public boolean remove(Object o){
        Entry<T> e = first;
        while(e != null){
            if(o.equals(e.data)){
                removeEntry(e);
                return true;
            }
            e = e.next;
        }
        return false;
    }

    //按迭代顺序将集合的元素追加到此列表的末尾
    public boolean addAll(Collection<?extends T> c){
        return addAll(size,c);
    }

    //在此列表的给定索引处按迭代顺序插入集合的元素
    public boolean addAll(int index,Collection<?extends T> c){
        checkBoundsInclusive(index);
        int csize = c.size();
        if(csize == 0){
            return false;
        }
        Iterator<?extends T> itr = c.iterator();

        Entry<T> after = null;
        Entry<T> before = null;
        if(index != size){
            after = getEntry(index);
            before = after.previous;
        }else{
            before = last;
        }

        //创建第一个新条目。我们还没有设置从“before”到第一个条目的链接，
        Entry<T> e = new Entry<T>(itr.next());
        e.previous = before;
        Entry<T> prev = e;
        Entry<T> firstNew = e;

        //创建并链接所有剩余条目。
        for(int pos = 1;pos < csize; pos++){
            e = new Entry<T>(itr.next());
            e.previous = prev;
            prev.next = e;
            prev = e;
        }

        //将新的条目链链接到列表中。
        modCount++;
        size += csize;
        prev.next = after;
        if(after != null){
            after.previous = e;
        }else{
            last = e;
        }
        if(before != null){
            before.next = firstNew;
        }else{
            first = firstNew;
        }
        return true;
    }

    //清空LinkedList
    public void clear(){
        if(size > 0 ){
            modCount++;
            first = null;
            last = null;
            size =0;
        }
    }

    //获取元素的下标
    public T get(int index){
        checkBoundsExclusive(index);
        return getEntry(index).data;
    }

    //替换列表中给定位置的元素。
    public T set(int index,T o){
        checkBoundsExclusive(index);
        Entry<T> e = getEntry(index);
        T old = e.data;
        e.data = o;
        return old;
    }

    //在列表中d 给定位置插入元素。
    public void add(int index ,T o){
        checkBoundsInclusive(index);
        Entry<T> e = new Entry<T>(o);
        if(index <size){
            modCount++;
            Entry<T> after = getEntry(index);
            e.next = after;
            e.previous = after.previous;
            if(after.previous == null){
                first = e;
            }else{
                after.previous.next = e;
            }
            after.previous = e;
            size ++;
        }else{
            addLastEntry(e);
        }
    }

    //从列表中删除位于给定位置的元素。
    public T remove(int index){
        checkBoundsExclusive(index);
        Entry<T> e = getEntry(index);
        removeEntry(e);
        return e.data;
    }

    //返回元素位于列表中的第一个索引，或-1
    public int indexOf(Object o){
        int index = 0;
        Entry<T> e = first;
        while(e != null){
            if(o.equals(e.data)){
                return index;
            }
            index ++;
            e = e.next;
        }
        return -1;
    }

    //返回元素位于列表中的最后一个索引，或-1。
    public int lastIndexOf(Object o){
        int index = size -1;
        Entry<T> e = last;
        while(e != null){
            if(o.equals(e.data)){
                return index;
            }
            index --;
            e = e.previous;
        }
        return -1;
    }

    //Line 630








 }
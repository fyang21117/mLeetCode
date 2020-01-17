/*
 * @lc app=leetcode.cn id=705 lang=java
 *
 * [705] 设计哈希集合
 * 2020年1月17日09:25:44
 */

// @lc code=start
class MyHashSet {

    /** Initialize your data structure here. */
    class Node{
        int val;
        Node prev,next;
        Node(int val){
            this.val = val;
        }
    }
    private int length = 100;
    private Node[]data = new Node[length];

    public MyHashSet() {
        
    }
    
    public void add(int key) {
        int index = key % length;
        Node curr = data[index];
        if(curr == null){
            Node node = new Node(key);
            data[index]=node;
            return;
        }
        while(true){
            if(curr.val == key)
            return;
            if(curr.next == null){
                Node node = new Node(key);
                node.prev = curr;
                curr.next = node;
                return;
            }else{
                curr = curr.next;
            }
        }
    }
    
    public void remove(int key) {
        int index = key%length;
        Node curr = data[index];
        if(curr != null && curr.val == key){
            Node next = curr.next;
            if(next != null){
                next.prev = null;
            }
            data[index ] = next;
            return;
        }
        while(curr != null){
            if(curr.val == key){
                Node next = curr.next;
                Node prev = curr.prev;
                if(next != null){
                    next.prev = prev;
                }
                if(prev != null){
                    prev.next = next;
                }
                return ;
            }
            curr = curr.next;
        }

    }
    
    /** Returns true if this set contains the specified element */
    public boolean contains(int key) {
        int index = key%length;
        Node curr = data[index];
        while(curr != null){
            if(curr.val == key){
                return true;
            }
            curr = curr.next;
        }
        return false;
    }
}

/**
 * Your MyHashSet object will be instantiated and called as such:
 * MyHashSet obj = new MyHashSet();
 * obj.add(key);
 * obj.remove(key);
 * boolean param_3 = obj.contains(key);
 */
// @lc code=end


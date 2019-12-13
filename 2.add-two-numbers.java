/*
 * @lc app=leetcode id=2 lang=java
 *
 * [2] Add Two Numbers
 */

// @lc code=start
/**
 * Definition for singly-linked list.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode(int x) { val = x; }
 * }
 */

class Solution {
    public class ListNode {
        int val;
        ListNode next;
        ListNode(int x) { val = x; }
    }
    
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {

        //2019-12-13,时间复杂度O(max(l1.length,l2.length))

        ListNode dummyHead = new ListNode(0);
        ListNode p = l1,q = l2,curr = dummyHead;
        int carry = 0;
        while(p!=null || q!=null){
            int x = (p!=null)?p.val:0;
            int y = (q!=null)?q.val:0;
            int sum = carry + x + y;
            carry = sum/10;
            curr.next = new ListNode(sum%10);
            curr = curr.next;

            if(p!=null) 
                p = p.next;
            if(q!=null)
                q = q.next;
        }
        if(carry > 0){
            curr.next = new ListNode(carry);
        }
        return dummyHead.next;
    }
}
// @lc code=end


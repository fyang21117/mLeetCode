import java.util.HashSet;
import java.util.Set;

/*
 * @lc app=leetcode id=3 lang=java
 *
 * [3] Longest Substring Without Repeating Characters
 */

// @lc code=start
class Solution {
    public int lengthOfLongestSubstring(String s) {
        // //2019-12-13，滑动窗口，O(n)
        // int n = s.length();
        // Set<Character> set = new HashSet<>();
        // int ans =0,i=0,j=0;
        // while(i<n && j<n){
        //     if(!set.contains(s.charAt(j))){
        //         set.add(s.charAt(j++));
        //         ans = Math.max(ans, j-i);
        //     }else{
        //         set.remove(s.charAt(i++));
        //     }
        // }
        // return ans;


        //优化，使用hashmap,时间复杂度O(n)
        //int [26] 用于字母 a-z,A-Z
        //int [128] 用于ASCII码
        //int [256] 用于扩展ASCII码
        //2019年12月25日17:16:36
        int n = s.length(),ans = 0;
        int []index = new int[128];
        for(int j=0,i=0;j<n;j++){
            i = Math.max(index[s.charAt(j)], i);
            ans = Math.max(ans, j-i+1);
            index[s.charAt(j)] = j+1;
        }
        return ans;
// Accepted
// 987/987 cases passed (2 ms)
// Your runtime beats 99.8 % of java submissions
// Your memory usage beats 91.75 % of java submissions (37.4 MB)
    }
}
// @lc code=end


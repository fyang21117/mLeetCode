/*
 * @lc app=leetcode id=344 lang=java
 *
 * [344] Reverse String
 */

// @lc code=start
class Solution {
    public void reverseString(char[] s) {
        if(s == null)return;
        for(int i=0,j=s.length-1;i<j;++i,--j){
            char tmp = s[i];
            s[i] = s[j];
            s[j] = tmp;
        }
    }
}
// @lc code=end


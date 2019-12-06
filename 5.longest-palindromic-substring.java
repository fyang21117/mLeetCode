/*
 * @lc app=leetcode id=5 lang=java
 *
 * [5] Longest Palindromic Substring
 */

// @lc code=start
class Solution {
    public String longestPalindrome(String s) {
        /**
         * 20191206
         * (1)嵌套循环，枚举i，j（起点和终点）,判断该子串是否回文
         * （2）中间向两边扩张法
         * （3）动态规划:DP[i][j]
         */

         int n = s.length();
         String res = "";
         boolean[][]dp = new boolean[n][n];

         for(int i=n-1;i>=0;i--){
             for(int j=i;j<n;j++){
                 dp[i][j] = s.charAt(i)==s.charAt(j)&&(j-i<2 || dp[i+1][j-1]);
                 if(dp[i][j]&& j-i+1>res.length()){
                     res = s.substring(i, j+1);
                 }
             }
         }
         return res;

    }
}
// @lc code=end


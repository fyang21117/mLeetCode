/*
 * @lc app=leetcode id=115 lang=java
 *
 * [115] Distinct Subsequences
 */

// @lc code=start
class Solution {
    public int numDistinct(String s, String t) {
        /**
         * 20191206
         * 解法1.暴力递归
         * 解法2.动态规划
         * dp[i][j]代表T前i字符串可以由s前j字符串组成最多个数
         * 动态方程：
         * if(s[j]==T[i])
         *  dp[i][j] = dp[i-1][j-1]+ dp[i][j-1]
         * else if (s[j]!=T[i])
         *  dp[i][j] = dp[i][j-1];
         * 
         */

         int [][]dp = new int[t.length()+1][s.length()+1];
         for(int i=0;i<s.length()+1;i++){
             dp[0][i]=1;
         }
         for(int i=1;i<t.length()+1;i++){
             for(int j=i;j<s.length()+1;j++){
                 if(t.charAt(i-1) == s.charAt(j-1)){
                     dp[i][j] = dp[i][j-1] + dp[i-1][j-1];
                 }else{
                     dp[i][j] = dp[i][j-1];
                 }
             }
         }
         return dp[t.length()][s.length()];
    }
}
// @lc code=end


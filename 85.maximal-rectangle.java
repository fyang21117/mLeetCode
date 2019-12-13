import java.util.Arrays;

/*
 * @lc app=leetcode id=85 lang=java
 *
 * [85] Maximal Rectangle
 */

// @lc code=start
class Solution {
    public int maximalRectangle(char[][] matrix) {
        
        // //2019-12-13我们预计算最大宽度的方法事实上将输入转化成了一系列的柱状图，
        // //每一栏是一个新的柱状图。我们在针对每个柱状图计算最大面积。
        // if(matrix.length == 0)
        //     return 0;
        // int maxarea = 0;
        // int [][]dp = new int[matrix.length][matrix[0].length];

        // for(int i=0;i<matrix.length;i++){
        //     for(int j=0;j<matrix[0].length;j++){
        //         if(matrix[i][j] == '1'){
        //             //计算最大的宽度
        //             dp[i][j] = j==0 ?1:(dp[i][j-1] + 1);

        //             int width = dp[i][j];

        //             //计算最大面积
        //             for(int k=i;k>=0;k--){
        //                 width = Math.min(width,dp[k][j]);
        //                 maxarea = Math.max(maxarea, width*(i-k+1));
        //             }
        //         }
        //     }
        // }
        // return maxarea;


        //2019-12-13,动态规划，时间复杂度O(M x N)
        //于每个点我们会通过以下步骤计算一个矩形：
        // 不断向上方遍历，直到遇到“0”，以此找到矩形的最大高度。
        // 向左右两边扩展，直到无法容纳矩形最大高度。
        if(matrix.length == 0 )
            return 0;
        int m = matrix.length;
        int n = matrix[0].length;

        int []left = new int[n];
        int []right = new int[n];
        int []height = new int[n];

        Arrays.fill(right,n);
        int maxarea = 0;
        for(int i=0;i<m;i++){
            int cur_left = 0,cur_right = n;

            //update height
            for(int j=0;j<n;j++){
                if(matrix[i][j] == '1')
                    height[j]++;
                else
                    height[j] = 0;
            }

            //update left
            for(int j=0;j<n;j++){
                if(matrix[i][j] == '1')
                    left[j]=Math.max(left[j], cur_left);
                else 
                    {
                        left[j] = 0;
                        cur_left = j+1;
                    }
            }

            //update right
            for(int j=n-1;j>=0;j--){
                if(matrix[i][j]=='1'){
                    right[j] = Math.min(right[j],cur_right);
                }else{
                    right[j] = n;
                    cur_right = j;
                }
            }

            //update area
            for(int j=0;j<n;j++){
                maxarea = Math.max(maxarea, (right[j]-left[j])*height[j]);
            }
           
        }
        return maxarea;
    }
}
// @lc code=end


/*
 * @lc app=leetcode id=63 lang=java
 *
 * [63] Unique Paths II
 */

// 算法:2019-12-13,动态规划，时间复杂度O（M x N）

//(1) 如果第一个格点 obstacleGrid[0,0] 是 1，说明有障碍物，那么机器人不能做任何移动，我们返回结果 0。
// (2)否则，如果 obstacleGrid[0,0] 是 0，我们初始化这个值为 1 然后继续算法。
// (3)遍历第一行，如果有一个格点初始值为 1 ，说明当前节点有障碍物，没有路径可以通过，设值为 0 ；否则设这个值是前一个节点的值 obstacleGrid[i,j] = obstacleGrid[i,j-1]。
// (4)遍历第一列，如果有一个格点初始值为 1 ，说明当前节点有障碍物，没有路径可以通过，设值为 0 ；
//否则设这个值是前一个节点的值 obstacleGrid[i,j] = obstacleGrid[i-1,j]。(动态方程)
// (5)现在，从 obstacleGrid[1,1] 开始遍历整个数组，如果某个格点初始不包含任何障碍物，就把值赋为上方和左侧两个格点方案数之和 obstacleGrid[i,j] = obstacleGrid[i-1,j] + obstacleGrid[i,j-1]。
// (6)如果这个点有障碍物，设值为 0 ，这可以保证不会对后面的路径产生贡献。



// @lc code=start
class Solution {
    public int uniquePathsWithObstacles(int[][] obstacleGrid) {
        int R = obstacleGrid.length;
        int C = obstacleGrid[0].length;

        if(obstacleGrid[0][0] == 1){
            return 0;
        }

        //开始遍历
        obstacleGrid[0][0] = 1;
        //遍历第一列
        for(int i=1;i<R;i++){
            obstacleGrid[i][0] = (obstacleGrid[i][0]==0 && obstacleGrid[i-1][0]==1)?1:0;
        }
        //遍历第一行
        for(int i=1;i<C;i++){
            obstacleGrid[0][i] = (obstacleGrid[0][i]==0 && obstacleGrid[0][i-1]==1)?1:0;
        }

        for(int i=1;i<R;i++){
            for(int j=1;j<C;j++){
                if(obstacleGrid[i][j] == 0){
                    obstacleGrid[i][j] = obstacleGrid[i-1][j] + obstacleGrid[i][j-1];
                }else{
                    obstacleGrid[i][j] = 0;
                }
            }
        }

        return obstacleGrid[R-1][C-1];

    }
}
// @lc code=end


import java.util.HashMap;
import java.util.Map;

/*
 * @lc app=leetcode id=1 lang=java
 *
 * [1] Two Sum
 */

// @lc code=start
class Solution {
    public int[] twoSum(int[] nums, int target) {
        //2019-12-13
        //使用一次hashmap
        Map<Integer,Integer>map = new HashMap<>();
        for(int i=0;i<nums.length;i++){
            int temp = nums[i] - target;
            if(map.containsKey(temp)){
                return new int[]{map.get(temp),i};
            }
            map.put(nums[i],i);
        }
        throw new IllegalArgumentException("NO two sum solution");
    }
}
// @lc code=end


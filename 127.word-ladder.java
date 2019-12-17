import javafx.util.*;
import java.util.*;

/*
 * @lc app=leetcode id=127 lang=java
 *
 * [127] Word Ladder
 */

// @lc code=start
class Solution {
    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        Set<String> set = new HashSet<>();
        for (String s : wordList) {
            set.add(s);
        }
        if (!set.contains(endWord)) return 0;
        Queue<String> queue = new LinkedList<>();
        queue.offer(beginWord);
        int step = 1;
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String s = queue.poll();
                char[] arr = s.toCharArray();
                for (int j = 0; j < arr.length; j++) {
                    char original = arr[j];
                    for (char c = 'a'; c <= 'z'; c++) {
                        if (c == arr[j]) continue;
                        arr[j] = c;
                        String test = String.valueOf(arr);
                        if (test.equals(endWord)) return step + 1;
                        if (set.contains(test)) {
                            queue.offer(test);
                            set.remove(test);
                        }
                    }
                    arr[j] = original;
                }
            }
            step++;
        }
        return 0;
    }
}
// @lc code=end


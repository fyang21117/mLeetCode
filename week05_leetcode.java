//【leetcode-1114按序打印 】
//我们提供了一个类：
// public class Foo {
//   public void one() { print("one"); }
//   public void two() { print("two"); }
//   public void three() { print("three"); }
// }
// 三个不同的线程将会共用一个 Foo 实例。
// 线程 A 将会调用 one() 方法
// 线程 B 将会调用 two() 方法
// 线程 C 将会调用 three() 方法
// 请设计修改程序，以确保 two() 方法在 one() 方法之后被执行，three() 方法在 two() 方法之后被执行。
// 示例 1:
// 输入: [1,2,3]
// 输出: "onetwothree"
// 解释: 
// 有三个线程会被异步启动。
// 输入 [1,2,3] 表示线程 A 将会调用 one() 方法，线程 B 将会调用 two() 方法，线程 C 将会调用 three() 方法。
// 正确的输出是 "onetwothree"。
// 示例 2:
// 输入: [1,3,2]
// 输出: "onetwothree"
// 解释: 
// 输入 [1,3,2] 表示线程 A 将会调用 one() 方法，线程 B 将会调用 three() 方法，线程 C 将会调用 two() 方法。
// 正确的输出是 "onetwothree"。
// 注意:
// 尽管输入中的数字似乎暗示了顺序，但是我们并不保证线程在操作系统中的调度顺序。
// 你看到的输入格式主要是为了确保测试的全面性。
class Foo {
    private boolean firstFinished;
    private boolean secondFinished;
    private Object lock = new Object();
    public Foo() {  }
    public void first(Runnable printFirst) throws InterruptedException {
        synchronized (lock) {
            // printFirst.run() outputs "first". Do not change or remove this line.
            printFirst.run();
            firstFinished = true;
            lock.notifyAll(); 
        }
    }
    public void second(Runnable printSecond) throws InterruptedException {
        synchronized (lock) {
            while (!firstFinished) {
                lock.wait();
            }
            // printSecond.run() outputs "second". Do not change or remove this line.
            printSecond.run();
            secondFinished = true;
            lock.notifyAll();
        }
    }
    public void third(Runnable printThird) throws InterruptedException {
        synchronized (lock) {
           while (!secondFinished) {
                lock.wait();
            }
            // printThird.run() outputs "third". Do not change or remove this line.
            printThird.run();
        } 
    }
}


//【LeetCode-1115】
// 我们提供一个类：
// class FooBar {
//   public void foo() {
//     for (int i = 0; i < n; i++) {
//       print("foo");
//     }
//   }
//   public void bar() {
//     for (int i = 0; i < n; i++) {
//       print("bar");
//     }
//   }
// }
// 两个不同的线程将会共用一个 FooBar 实例。其中一个线程将会调用 foo() 方法，另一个线程将会调用 bar() 方法。
// 请设计修改程序，以确保 "foobar" 被输出 n 次。
// 示例 1:
// 输入: n = 1
// 输出: "foobar"
// 解释: 这里有两个线程被异步启动。其中一个调用 foo() 方法, 另一个调用 bar() 方法，"foobar" 将被输出一次。
// 示例 2:
// 输入: n = 2
// 输出: "foobarfoobar"
// 解释: "foobar" 将被输出两次。
//执行结果：通过显示详情(待优化)
//执行用时 :25 ms, 在所有 Java 提交中击败了23.46%的用户
class FooBar {
    private int n;
    private volatile int flag = 0;
	public FooBar(int n) {
		this.n = n;
	}
	public void foo(Runnable printFoo) throws InterruptedException {
         for (int i = 0; i < n; i++) {
			while(flag != 0){
				Thread.yield();
			}
			printFoo.run();
			flag = 1;
		}
	}
	public void bar(Runnable printBar) throws InterruptedException {
        for (int i = 0; i < n; i++) {
			while(flag != 1){
				Thread.yield();
			}
			printBar.run();
			flag = 0;
		}
	}
}


// LeetCode-1116打印零与奇偶数
// 假设有这么一个类：
// class ZeroEvenOdd {
//   public ZeroEvenOdd(int n) { ... }      // 构造函数
//   public void zero(printNumber) { ... }  // 仅打印出 0
//   public void even(printNumber) { ... }  // 仅打印出 偶数
//   public void odd(printNumber) { ... }   // 仅打印出 奇数
// }
// 相同的一个 ZeroEvenOdd 类实例将会传递给三个不同的线程：
// 线程 A 将调用 zero()，它只输出 0 。
// 线程 B 将调用 even()，它只输出偶数。
// 线程 C 将调用 odd()，它只输出奇数。
// 每个线程都有一个 printNumber 方法来输出一个整数。请修改给出的代码以输出整数序列 010203040506... ，其中序列的长度必须为 2n。
// 示例 1：
// 输入：n = 2
// 输出："0102"
// 说明：三条线程异步执行，其中一个调用 zero()，另一个线程调用 even()，最后一个线程调用odd()。正确的输出为 "0102"。
// 示例 2：
// 输入：n = 5
// 输出："0102030405"
class ZeroEvenOdd {
	private int n;
	private boolean ifZeroDone = false;
	private boolean ifEvenOddDone = false;
	public ZeroEvenOdd(int n) {
		this.n = n;
	}
	// printNumber.accept(x) outputs "x", where x is an integer.
	public void zero(IntConsumer printNumber) throws InterruptedException {
		for (int i = 0; i < n; i++) {
			synchronized (this) {
				while (ifZeroDone) {
					this.wait();
				}
				printNumber.accept(0);
				ifZeroDone = true;
				this.notifyAll();
			}
		}
	}
	public void even(IntConsumer printNumber) throws InterruptedException {
		for (int j = 2; j <= n; j = j + 2) {
			synchronized (this) {
				while (!ifZeroDone || !ifEvenOddDone) {
					this.wait();
				}
				printNumber.accept(j);
				ifZeroDone = false;
				ifEvenOddDone = false;
				this.notifyAll();
			}
		}
	}
	public void odd(IntConsumer printNumber) throws InterruptedException {
		for (int j = 1; j <= n; j = j + 2) {
			synchronized (this) {
				while (!ifZeroDone || ifEvenOddDone) {
					this.wait();
				}
				printNumber.accept(j);
				ifZeroDone = false;
				ifEvenOddDone = true;
				this.notifyAll();
			}
		}
	}
}
//https://leetcode-cn.com/problemset/concurrency/
//leetcode-1117 H20生成
//leetcode-1195. 交替打印字符串
//leetcode-1226. 哲学家进餐
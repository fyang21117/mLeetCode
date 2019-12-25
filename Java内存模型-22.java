
/**
 * 【参考资料】
 * 来源：《深入理解Java虚拟机》第十二章
 * 2019年12月23日20:53:37
 * 
 * 内存模型（Memory Model） 【含义】：
 * 在特定的操作协议（eg：MSI,MESI,MOSI,Synapse,Firefly,Dragon Protocol）下，对特定的内存或高速缓存进行读写访问的过程抽象。
 * 不同架构的物理机器拥有不同的内存模型，Java虚拟机也有自己的内存模型。
 * 
 * 
 * 
 * Java内存模型（JMM）
 * 【定义】：屏蔽掉各种硬件和操作系统的内存访问差异，以实现让Java程序在各种平台下都能达到一致的内存访问效果。
 * 
 * 【主要目标】：定义程序中各个变量的访问规则，即在虚拟机中将变量存储到内存和从内存中取出变量这样的底层细节。
 * 此处的变量包括了实例字段、静态字段和构成数组对象的元素，但不包括局部变量与方法参数（线程私有，不被共享，不存在竞争问题）。
 * 
 * 【规定】：
 * （1）所有的变量存储在主内存，每条线程有自己的工作内存，其中保存了该线程使用到的变量的主内存副本拷贝，
 * （2）线程对变量的所有操作（读取、赋值等）都必须在工作内存中进行，而不能直接读写主内存中的变量。
 * （3）不同线程之间也不能直接访问对方工作内存中的变量，线程之间的变量值传递均需要通过主内存来完成。交互关系如下所示：
 * 
 *     【Java线程①】---->【工作内存①】---->【Save 和 Load操作】---->【主内存】
 *     【Java线程②】---->【工作内存②】---->【Save 和 Load操作】---->【主内存】
 *  
 * 其中，工作内存对应于虚拟机栈中的部分区域，主内存对应Java堆中的对象实例数据部分。
 * 
 * 
 * 【内存间交互操作】
 * Java内存模型定义了8种操作来实现一个变量如何从主内存拷贝到工作内存（read&load）、如何从工作内存同步回主内存（store&write）等细节。
 * 虚拟机实现时保证每一种操作都是原子的（Atomic）、不可再分的（Indivisible）。
 * （1）lock（锁定）：作用于主内存的变量，它把一个变量标识为一条线程独占的状态。
 * （2）unlock（解锁）：作用于主内存的变量，它把一个处于锁定状态的变量释放出来，释放后的变量才可以被其他线程锁定。
 * （3）read（读取）：作用于主内存的变量，它把一个变量的值从主内存中传输到线程的工作内存中，以便随后的load操作。
 * （4）load（载入）：作用于工作内存的变量，它把read操作从主内存中得到的变量值放入工作内存的变量副本中。
 * （5）use（使用）：作用于工作内存的变量，它把工作内存中的一个变量的值传递给执行引擎，
 * 每当虚拟机遇到一个需要使用到变量的值的字节码指令时将会执行这个操作。
 * （6）assign（赋值）：作用于工作内存的变量，它把一个从执行引擎接收到的值赋给工作内存的变量，
 * 每当虚拟机遇到一个给变量赋值的字节码指令时执行这个操作。
 * （7）store（存储）：作用于工作内存的变量，它把工作内存中一个变量的值传送到主内存中，以便随后的write操作使用。
 * （8）write（写入）：作用于主内存的变量，它把store操作从工作内存中得到的变量的值放入主内存的变量中。
 *.
 * 注意：read&load和store&write都必须按顺序执行，不一定连续执行。
 * 规则限定（确定Java程序中哪些内存访问操作在并发下是安全的）：
 * （1）不允许read&load、store&write操作之一单独出现。即不允许一个变量从主内存读取了但工作内存不接受，或者从工作内存发起回写但主存不接受的情况。
 * （2）不允许一个线程丢弃它的最近的assign操作，即变量在工作内存中改变了必须把该变化同步回主内存。
 * （3）不允许一个线程无原因地把数据从线程的工作内存同步回主内存（没有任何assign操作）。
 * （4）一个新的变量只能在主内存中“诞生”，不允许在工作内存中直接使用一个未被初始化（load&assign）的变量。
 * （5）一个变量在同一个时刻只允许一条线程对其进行lock操作，但lock操作可以被同一条线程重复执行多次；只有执行相同次数的unlock操作，变量才能被解锁。
 * （6）如果对一个变量执行lock操作，那将会清空工作内存中此变量的值。在执行引擎使用这个变量前，需要重新执行load或assign操作初始化变量的值。
 * （7）如果一个变量没有先被lock操作锁定，则不允许执行unlock操作，也不允许unlock一个被其他线程锁定的变量。
 * （8）对一个变量执行unlock操作之前，必须先把此变量同步回主内存中（store&write）。
 * 
 *  
 * 【效能优化】：Java内存模型没有限制执行引擎使用处理器的特定寄存器或缓存来和主内存进行交互，没有限制即时编译器进行调整代码执行顺序这类优化措施。
 * */
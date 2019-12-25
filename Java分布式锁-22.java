/*
 * 【参考链接】
 * 2019年12月25日09:35:01
 * 死磕 java同步系列之mysql分布式锁：https://mp.weixin.qq.com/s/Au-_hN-FcL30bIYQbLfZEQ
 * 一文了解分布式锁：https://www.jianshu.com/p/31d3de863ff7
 * 
 * 
 * 【定义】
 * 分布式锁，是指在分布式的部署环境下，通过锁机制来让多客户端互斥的对共享资源进行访问。
 * 
 * 
 * 【实现方案】
 * 1、基于数据库实现：唯一索引天然具有排他性，同一时刻只能允许一个竞争者获取锁。
 * （1）乐观锁机制其实就是在数据库表中引入一个版本号（version）字段来实现的。
 * （2）在Mysql中是基于 for update 来实现加锁的。
 * 
 * 2、基于Redis实现的锁机制：依赖redis自身的原子操作。
 * 
 * 3、基于Zookeeper实现：使用它的临时有序节点来实现的分布式锁。
 *      原理就是：当某客户端要进行逻辑的加锁时，就在zookeeper上的某个指定节点的目录下，去生成一个唯一的临时有序节点，
 *  然后判断自己是否是这些有序节点中序号最小的一个，如果是，则算是获取了锁。如果不是，则说明没有获取到锁，
 * 那么就需要在序列中找到比自己小的那个节点，并对其调用exist()方法，对其注册事件监听，
 * 当监听到这个节点被删除了，那就再去判断一次自己当初创建的节点是否变成了序列中最小的。
 * 如果是，则获取锁，如果不是，则重复上述步骤。
 * 
 * 
 * 【满足要求】
 * 排他性：在同一时间只会有一个客户端能获取到锁，其它客户端无法同时获取
 * 避免死锁：这把锁在一段有限的时间之后，一定会被释放（正常释放或异常释放）
 * 高可用：获取或释放锁的机制必须高可用且性能佳
 */



/***
 * 【分布式】基于Redis实现分布式锁:https://zhuanlan.zhihu.com/p/62274137
 * 【特性】
 * 安全特性：互斥访问，即永远只有一个 client 能拿到锁
 * 避免死锁：最终 client 都可能拿到锁，不会出现死锁的情况，即使原本锁住某资源的 client crash 了或者出现了网络分区
 * 容错性：只要大部分 Redis 节点存活就可以正常提供服务
 * 
 * Redlock接口实现类，我们可以看到redission封装的redlock算法实现的分布式锁用法，
 * 非常简单，跟重入锁（ReentrantLock）有点类似：
 */

 /**
 * Redlock 实现类
 */
@Component
public class RedissonDistributedLocker implements DistributedLocker {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 拿不到lock就不罢休，不然线程就一直block
     * @param lockKey
     * @return
     */
    @Override
    public RLock lock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        return lock;
    }
    /**
     * @param lockKey
     * @param timeout 加锁时间 单位为秒
     * @return
     */
    @Override
    public RLock lock(String lockKey, long timeout) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(timeout, TimeUnit.SECONDS);
        return lock;
    }
    /**
     * @param lockKey
     * @param unit  时间单位
     * @param timeout 加锁时间
     * @return
     */
    @Override
    public RLock lock(String lockKey, TimeUnit unit, long timeout) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(timeout, unit);
        return lock;
    }
    /**
     * tryLock()，马上返回，拿到lock就返回true，不然返回false。
     * 带时间限制的tryLock()，拿不到lock，就等一段时间，超时返回false.
     * @param lockKey
     * @param unit
     * @param waitTime
     * @param leaseTime
     * @return
     */
    @Override
    public boolean tryLock(String lockKey, TimeUnit unit, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }
    @Override
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.unlock();
    }
    @Override
    public void unlock(RLock lock) {
        lock.unlock();
    }
}
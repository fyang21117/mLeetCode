/***
 * 2020年1月30日09:30:36
 * 【初识MyBatis3】
 * 中文链接：https://mybatis.org/mybatis-3/zh/getting-started.html
 * 官方链接：https://mybatis.org/mybatis-3/
 * 
 * 参考链接：
 * https://github.com/mybatis/mybatis-3
 * https://blog.csdn.net/weixin_33924220/article/details/86044180
 */

 /***
  * 【什么是 MyBatis？】
  MyBatis 是一款优秀的持久层框架，它支持定制化 SQL、存储过程以及高级映射。
  MyBatis 避免了几乎所有的 JDBC 代码和手动设置参数以及获取结果集。
  MyBatis 可以使用简单的 XML 或注解来配置和映射原生类型、接口和 Java 的 POJO（Plain Old Java Objects，普通老式 Java 对象）为数据库中的记录。
  */



  /**
   * 【模块一：异常】
   * Mybatis这方面主要是处理多线程抛出的错误信息或者异常。ExceptionFactore简单的包装了普通的异常，抛出成Mybatis的异常PersistenceException。
   * -Line_SEPARATOR：换行符；
   * -LOCAL:ThreadLocal多线程安全变量
   * -stored：ErrorContent临时储存的错误信息
   * --source
   * --activity
   * --Object
   * --message
   * --sql
   * --cause
   * -instance（）：获取一个实例
   * -store（）：把errorContext从LOCAL存放到stored
   * -recall（）：把errorContext从stored存放到LOCAL
   */



    /**
   * 【模块二：缓存】
   * 缓存都是通过key、value形式存入的。Mybatis实现了一个类CacheKey来作为key值，主要是通过一个算法来计算一个值作为Key，如果重复了再通过每个对象的类型来一一比对进行区分。
   * ​ 然后这边的适配器有很多种，主要可以分为4类。
   * （1）回收机制适配器
   * a.堵塞队列缓存BlockingCache:​ 通过可重入锁（ReentrantLock）来实现堵塞，只有当前线使用完这个缓存之后才会释放资格锁，其他线程才能进行竞争。
   * b.先进先出缓存FifoCache:​ 通过Deque这个双向链表来实现先进先出的机制，缓存长度默认是1024。
   * c.最近最少使用缓存LruCache:额外又使用了一个Map来做lru机制，所以使用这种缓存机制会占用2倍的内存。​ 使用的Map是使用了LinkedHashMap，这个Map就是会每次访问或者插入一个新元素都会把元素放到链表的末尾。
   * d.定时调度缓存ScheduledCache:每隔固定的clearInterval就清理一次缓存。
   * 
   * （2）回收机制优化缓存:软引用缓存 + 弱引用缓存 + 序列化缓存
   * 
   * （3）事务缓存：一次存入多个缓存。或者移除多个缓存。主要是搭配一个事务缓存管理器进行统一的管理。​ 实现就是通过一个HashMap进行统一的管理。
   * 
   * （4）调试型缓存-日志缓存
   * //目的就是getObject时，打印命中率
@Override
public Object getObject(Object key) {
    //访问一次requests加一
    requests++;
    final Object value = delegate.getObject(key);
    //命中了则hits加一
    if (value != null) {
        hits++;
    }
    if (log.isDebugEnabled()) {
        //就是打印命中率 hits/requests
        log.debug("Cache Hit Ratio [" + getId() + "]: " + getHitRatio());
    }
    return value;
}
   * 
   */



   /**
    * 【模块三：解析】
    （1）字符串替换解析模块：实现方式就是GenericTokenParser这个类提供了parse方法来进行处理，通过传入的TokenHandler的实现类不同来处理不同的标记。
    （2）XML文件的读取和解析：使用的模块是org.w3c.dom.Node以及XPath这两个模块。 主要是用Node来解析XML文件的接口，然后使用XPath来替换字符串里面的数据。
    */



    /**
     * 【模块四：类型处理器】
     * 当MyBatis为PreparedStatement 设置参数时或者从ResultSet中获取数据时，会根据Java类型使用TypeHandler 去获取相应的值。
     * 1、TypeHandler接口：这个接口有三个方法，一个set，用来给PreparedStatement对象对应的列设置参数；两个get,从ResultSet和CallableStatement获取对应列的值,不同之处是一个是取第几个位置的值，一个是取具体列名所对应的值
     * 2、BaseTypeHandler抽象类：BaseTypeHandler类便是对TypeHandler接口的初步实现，在实现TypeHandler接口的三个函数外，又引入了3个抽象函数用于null值的处理。
     * 3、DateTypeHandler：在type包中有十多个具体的类来具体处理类型转换，每一个类处理一个数据类型，像long、int、double等等。
     * 
     */




     /**
      * 【模块五：IO】
      （1）VFS：虚拟文件系统，主要是用来读取服务器里的资源，这边主要是用来读取jar内的文件内容。
      (2)Resource：主要用来加载xml、props以及获取类文件的url等等。
      (3)ResolverUtil：主要两个check方法：IsA：是否是type或者是其父类；AnnotatedWith：这个元素上是否含有这个注解。
      */

      /**
       * 【模块六：数据源】
       * (1)unpooledDataSource提供了非池化的数据库连接，主要用常见的JDBC代码如下所示：
private Connection doGetConnection(Properties properties) throws SQLException {
    initializeDriver();
    Connection connection = DriverManager.getConnection(url, properties);
    configureConnection(connection);
    return connection;
}
private synchronized void initializeDriver() throws SQLException {
    if (!registeredDrivers.containsKey(driver)) {
        Class<?> driverType;
        try {
            if (driverClassLoader != null) {
                driverType = Class.forName(driver, true, driverClassLoader);
            }
            else {
                driverType = Resources.classForName(driver);
            }
            Driver driverInstance = (Driver)driverType.newInstance();
            DriverManager.registerDriver(new DriverProxy(driverInstance));
            registeredDrivers.put(driver, driverInstance);
        }
        catch (Exception e) {
            throw new SQLException("Error setting driver on UnpooledDataSource. Cause: " + e);
        }
    }
}
private void configureConnection(Connection conn) throws SQLException {
    if (autoCommit != null && autoCommit != conn.getAutoCommit()) {
        conn.setAutoCommit(autoCommit);
    }
    if (defaultTransactionIsolationLevel != null) {
        conn.setTransactionIsolation(defaultTransactionIsolationLevel);
    }
}

       * (2)PooledDataSource代码如下：
class PooledConnection implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        //如果调用close的话，忽略它，反而将这个connection加入到池中
        if (CLOSE.hashCode() == methodName.hashCode() && CLOSE.equals(methodName)) {
            dataSource.pushConnection(this);
            return null;
        }
        else {
            try {
                if (!Object.class.equals(method.getDeclaringClass())) {
                    // issue #579 toString() should never fail
                    // throw an SQLException instead of a Runtime
                    //除了toString()方法，其他方法调用之前要检查connection是否还是合法的,不合法要抛出SQLException
                    checkConnection();
                }
                //其他的方法，则交给真正的connection去调用
                return method.invoke(realConnection, args);
            }
            catch (Throwable t) {
                throw ExceptionUtil.unwrapThrowable(t);
            }
        }
    }
}

       * (3)JndiDataSource：这个数据源的实现是为了使用如 Spring 或应用服务器这类的容器, 容器可以集中或在外部配置数据源,然后放置一个 JNDI 上下文的引用。
       * ​ JNDI参考： https://yq.aliyun.com/articles/270917
       */



       /**
        * 【模块七：事务】
        在MYbatis中一共有两种事务管理器类型JDBC以及MANAGED。
        （1）JDBC直接利用JDBC的commit和rollback来进行事务管理，依赖于从数据源获得的连接来管理事务范围。
        （2）​ 另外一种是托管事务，就是交给容器来管理事务
        */




        /**
         * 【模块八：反射】
         * （1）对象工厂：所有对象都要由工厂来产生，Mybatis提供了一个默认的对象工厂，主要使用create接口。代码如下：
@Override
public <T> T create(Class<T> type) {
     return create(type, null, null);
}
 @SuppressWarnings("unchecked")
@Override
public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    //根据接口创建具体的类
    //1.解析接口
    Class<?> classToCreate = resolveInterface(type);
    // we know types are assignable
    //2.实例化类
    return (T)instantiateClass(classToCreate, constructorArgTypes, constructorArgs);
}

         * （2）调用者：​ 主要就是封装了一下get、set、method等的反射方法。实现接口Invoker的有GetFieldInvoker、MethodInvoker、SetFieldInvoker。
         * （3）wrapper：这边对象包装器，封装了反射相关的操作。抽出统一的抽象接口ObjectWrapper；​ 
         * 然后对于集合类，Mybatis暂时不提供支持，所以仅仅是在方法里面抛出异常，但是又在BaseWrapper里面简单支持了集合类的get、set方法。
         * ​ 最重要的实现类就是BeanWrapper，基本提供了初始化bean，set、get等相关操作。其中主要的实现是使用了MetaObject以及MetaClass来实现。
         */



         /**
          * 【模块九：session】
          session基本上是应用程序访问最长用的一个接口。一个session的产生顺序如下：
        a、根据配置的xml文件或者其他流信息，通过SqlSessionFactoryBuilder来创建一个SqlSessionFactory。
        b、通过SqlSessionFactory来开启一个Session。
          (1)DefaultSqlSessionFactory
          (2)DefaultSqlSession
          (3)Configuration
          */

          /**
           * 【设计模式】
           * 参考链接：https://blog.csdn.net/qq_35807136/article/details/79931345
           * 不过还没有来得及细看！！！
           * 
           * 
           * Mybatis至少遇到了以下的设计模式的使用：
（1）Builder模式，例如SqlSessionFactoryBuilder、Environment;
（2）工厂方法模式，例如SqlSessionFactory、TransactionFactory、TransactionFactory、LogFactory、ObjectFactory、ReflectorFactory;
（3）单例模式，例如ErrorContext和LogFactory；
（4）代理模式，Mybatis实现的核心，比如MapperProxy、ConnectionLogger，用的jdk的动态代理；还有executor.loader包使用了cglib或者javassist达到延迟加载的效果；
（5）组合模式，例如SqlNode和各个子类ChooseSqlNode等；
（6）模板方法模式，例如BaseExecutor和SimpleExecutor，还有BaseTypeHandler和所有的子类例如IntegerTypeHandler；
（7）适配器模式，例如Log的Mybatis接口和它对jdbc、log4j等各种日志框架的适配实现；
（8）装饰者模式，例如Cache包中的cache.decorators子包中等各个装饰者的实现；
（9）迭代器模式，例如迭代器模式PropertyTokenizer；
           * 
           */
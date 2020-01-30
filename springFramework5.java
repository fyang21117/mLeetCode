/**
 * 【SpringFramework5】
 * 首次接触spring相关知识，从源码入手。参考GitHub源码链接：
 * https://github.com/spring-projects/spring-framework
 * https://www.cnblogs.com/java-chen-hao/p/11046190.html
 * 
 * 【21模块介绍】
 * 
 * 【spring-aop】
 * spring-aop 是 Spring 的另一个核心模块，是 AOP 主要的实现模块。作为继 OOP 后，对程序员影
响最大的编程思想之一，AOP 极大地开拓了人们对于编程的思路。在 Spring 中，他是以 JVM 的动态代
理技术为基础，然后设计出了一系列的 AOP 横切实现，比如前置通知、返回通知、异常通知等，同时，
Pointcut 接口来匹配切入点，可以使用现有的切入点来设计横切面，也可以扩展相关方法根据需求进
行切入。
 * 
 * 
 * 【spring-aspects】
 * spring-aspects 模块集成自 AspectJ 框架，主要是为 Spring AOP 提供多种 AOP 实现方法。
 * 
 * 
 * 【spring-beans】和【spring-core】
 * spring-beans 和 spring-core 模块是 Spring 框架的核心模块，包含了控制反转（Inversion of
Control, IOC）和依赖注入（Dependency Injection, DI）。BeanFactory 接口是 Spring 框架中
的核心接口，它是工厂模式的具体实现。BeanFactory 使用控制反转对应用程序的配置和依赖性规范与
实际的应用程序代码进行了分离。但 BeanFactory 容器实例化后并不会自动实例化 Bean，只有当 Bean
被使用时 BeanFactory 容器才会对该 Bean 进行实例化与依赖关系的装配。
 * 
 * 
 * 【spring-context-indexer】
 * spring-context-indexer 模块是 Spring 的类管理组件和 Classpath 扫描。
 * 
 * 
 * 【spring-context-support】
 * spring-context-support 模块是对 Spring IOC 容器的扩展支持，以及 IOC 子容器。
 * 
 * 
 * 【spring-context】
 * spring-context 模块构架于核心模块之上，他扩展了 BeanFactory，为她添加了 Bean 生命周期
控制、框架事件体系以及资源加载透明化等功能。此外该模块还提供了许多企业级支持，如邮件访问、
远程访问、任务调度等，ApplicationContext 是该模块的核心接口，她是 BeanFactory 的超类，与
BeanFactory 不同，ApplicationContext 容器实例化后会自动对所有的单实例 Bean 进行实例化与
依赖关系的装配，使之处于待用状态。
 *
 * 
 * 【spring-expression】
 * spring-expression 模块是统一表达式语言（EL）的扩展模块，可以查询、管理运行中的对象，
同时也方便的可以调用对象方法、操作数组、集合等。它的语法类似于传统 EL，但提供了额外的功能，
最出色的要数函数调用和简单字符串的模板函数
 * 
 * 
 * 【spring-instrument】
 * spring-instrument 模块是基于 JAVA SE 中的"java.lang.instrument"进行设计的，应该算是
AOP 的一个支援模块，主要作用是在 JVM 启用时，生成一个代理类，程序员通过代理类在运行时修改类
的字节，从而改变一个类的功能，实现 AOP 的功能。


 * 【spring-jcl】
 * JCL全称：Jakarta Commons Logging
spring-jcl 采用了设计模式中的“适配器模式”，它对外提供统一的接口，然后在适配类中将对日志的操作委托给具体的日志框架。
 * 
 * 【spring-jdbc】
 * spring-jdbc 模块是 Spring 提供的 JDBC 抽象框架的主要实现模块，用于简化 Spring JDBC。主
要是提供 JDBC 模板方式、关系数据库对象化方式、SimpleJdbc 方式、事务管理来简化 JDBC 编程，主
要实现类是 JdbcTemplate、SimpleJdbcTemplate 以及 NamedParameterJdbcTemplate。
 * 
 * 
 * 【spring-jms】
 * spring-jms 模块（Java Messaging Service）能够发送和接受信息，自 Spring Framework 4.1
以后，他还提供了对 spring-messaging 模块的支撑
 * 
 * 
 * 【spring-messaging】
 * spring-messaging 是从 Spring4 开始新加入的一个报文发送模块，主要职责是为 Spring 框架集成一些基
础的报文传送应用。
 * 
 * 
 * 【spring-orm】
 * ORM模块为对象-关系映射API，提供一个交互层，利用ORM封装包，可以混合使用所有Spring提供的特性进行O/R映射。
 * 
 * 
 * 【spring-oxm】
 * spring-oxm 模块主要提供一个抽象层以支撑 OXM（OXM 是 Object-to-XML-Mapping 的缩写，它是
一个 O/M-mapper，将 java 对象映射成 XML 数据，或者将 XML 数据映射成 java 对象），例如：JAXB,
Castor, XMLBeans, JiBX 和 XStream 等。
 * 
 * 
 * 【spring-test】
 * test模块支持使用Junit和TestNG对spring组件进行测试。
 * 
 * 
 * 【spring-tx】
 * spring-tx 模块是 Spring JDBC 事务控制实现模块。使用 Spring 框架，它对事务做了很好的封装，
通过它的 AOP 配置，可以灵活的配置在任何一层；但是在很多的需求和应用，直接使用 JDBC 事务控制
还是有其优势的。


 * 【spring-web】
 * spring-web 模块为 Spring 提供了最基础 Web 支持，主要建立于核心容器之上，通过 Servlet 或
者 Listeners 来初始化 IOC 容器，也包含一些与 Web 相关的支持。
 * 
 * 
 * 【spring-webflux】
 * spring-webflux 是一个新的非堵塞函数式 Reactive Web 框架，可以用来建立异步的，非阻塞，
事件驱动的服务，并且扩展性非常好。
 * 
 * 
 * 【spring-webmvc】
 * spring-webmvc 模 块 众 所 周 知 是 一 个 的 Web-Servlet 模 块 ， 实 现 了 Spring MVC
（model-view-Controller）的 Web 应用。
 * 
 * 
 * 【spring-websocket】
 * spring-websocket 模块主要是与 Web 前端的全双工通讯的协议。
 * 
 */



 /**
  * 【SpringFramework5架构图】
    |-----------------------------------------------------------------
    | [数据访问集成 ]            [网络访问]
    | JDBC----ORM               web----webflux
    | OXM----JMS                webmvc----websocket
    | Transactions    
    |
    |         AOP----Aspects----Instrument----Messaging
    |
    | [核心容器]
    | Beans----Core----Context----Expression
    |           Test
    |-------------------------------------------------------------------
  */
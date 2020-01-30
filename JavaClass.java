
    /**
     *【class源码部分解析】
     * 这里只是class的部分解析。
     * 2020年1月17日17:46:34
     */
public final    class Class<T> implements java.io.Serializable,
                            java.lang.reflect.GenericDeclaration,
                              java.lang.reflect.Type,
                              java.lang.reflect.AnnotatedElement {

    private static final long serialVersionUID = 1L;
    private static final int ANNOTATION = 0x00002000;// 注释类型
    private static final int ENUM      = 0x00004000;//枚举类型
    private static final int SYNTHETIC = 0x00001000;//合成类型，注0

    //枚举是一种类（class），注释是一种接口（interface）
    private transient String name;
    //全限定名（包+类名）
    private native String getName0();
    //本地方法获取name属性

    //注册本地方法
    private static native void registerNatives();
    static {
        registerNatives();
    }

    private Class() {}
    //唯一私有构造方法，说明Class不可由用户构造实例

    public String toString() {
        //这里重写toString，只区别了接口和Class
        return (isInterface() ? "interface " : (isPrimitive() ? "" : "class "))
            + getName();
    }

    //通过类全限定名获得该类（或接口）的Class对象（加载该类）
    @CallerSensitive   //注1
    public static Class<?> forName(String className)
                throws ClassNotFoundException {
        Class<?> caller = Reflection.getCallerClass();
        return forName0(className, true, ClassLoader.getClassLoader(caller), caller);
    }

    @CallerSensitive
    public static Class<?> forName(String name, boolean initialize,
                                   ClassLoader loader)
        throws ClassNotFoundException{
        //initialize : 是否立即初始化该类，注2
        //loader : 使用指定的类加载器加载
    }
    private static native Class<?> forName0(String name, boolean initialize,
                                            ClassLoader loader,
                                            Class<?> caller)
        throws ClassNotFoundException;
    //反射获得该类实例对象
 @CallerSensitive
    public T newInstance()
        throws InstantiationException, IllegalAccessException{
    //JDK明言：本方法在当前Java内存模型下不一定是正确的
        //反射获取该类实例对象，实例化对象不是通过new指令，而是直接
        //通过本地方法获取类的公有构造方法（无参），然后通过Constructor的
        //newInstance();方法实例化对象
        //这个过程还要检查是否有权限反射实例化该对象
    }

    //缓存上面方法已获取的公有构造方法，供下次使用
    private volatile transient Constructor<T> cachedConstructor;

    //缓存调用本方法的最初对象的类Class对象，供安全检查使用，见注1
    private volatile transient Class<?>       newInstanceCallerCache;

    //判断obj对象是否是该Class的实例
    public native boolean isInstance(Object obj);

    //判断cls是否是调用者同一类型或其子类类型
    public native boolean isAssignableFrom(Class<?> cls);

    //判断该Class是否是接口类型
    public native boolean isInterface();

    //判断该Class是否是数组类型
    public native boolean isArray();

    //判断该Class是否是基本类型，注3（八大基本类型+一特殊基本类型）
    public native boolean isPrimitive();

    //判断该Class是否是注释类型
    public boolean isAnnotation() {
        return (getModifiers() & ANNOTATION) != 0;
    }

    //判断该Class是否是合成类型
    public boolean isSynthetic() {
        return (getModifiers() & SYNTHETIC) != 0;
    }

    public String getName() {
        String name = this.name;
        if (name == null)
            this.name = name = getName0();//通过本地方法获取全路径类名
        return name;
    }

    //获取加载该类的类加载器
    @CallerSensitive
    public ClassLoader getClassLoader() {
        ClassLoader cl = getClassLoader0();
        if (cl == null)
            return null;
        SecurityManager sm = System.getSecurityManager();//注5
        if (sm != null) {
            ClassLoader.checkClassLoaderPermission(cl, Reflection.getCallerClass());
        }
        return cl;
    }
    native ClassLoader getClassLoader0();

    //返回该类中变量字段的类型变量数组，按声明顺序排序，注4
    public TypeVariable<Class<T>>[] getTypeParameters() {
        if (getGenericSignature() != null)
            return (TypeVariable<Class<T>>[])getGenericInfo().getTypeParameters();
        else
            return (TypeVariable<Class<T>>[])new TypeVariable<?>[0];
    }

    //获得该类的直接父类的Class对象，如果该类是接口，则返回null
    public native Class<? super T> getSuperclass();

    //返回带参数化类型的直接父类的类型
    public Type getGenericSuperclass() {}

    //获取该类的包路径
    public Package getPackage() {
        return Package.getPackage(this);
    }

    //获取该类直接实现的所有接口
    public native Class<?>[] getInterfaces();

    //获取所有接口，同上面的不同之处在于，若超接口是参数化类型（泛型）则返回的是其实际类型
    public Type[] getGenericInterfaces() {
        if (getGenericSignature() != null)
            return getGenericInfo().getSuperInterfaces();
        else
            return getInterfaces();
    }

    /**返回数组类型，若该类不是数组，返回null
     * 如：A[] a = new A[];
     * a.getClass().getComponentType()返回的是A类型（全路径名） 
     */
    public native Class<?> getComponentType();

    //返回这个类的修饰符的对应的int值，二者可通过Modifier.toString()转换
    public native int getModifiers();

    //获取该类的所有签名（标记）列表
    public native Object[] getSigners();
    
    //设置该类的签名，注意方法修饰是默认，所以只有同包下类可用
    native void setSigners(Object[] signers);
}
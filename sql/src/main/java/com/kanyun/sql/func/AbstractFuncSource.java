package com.kanyun.sql.func;

import com.kanyun.sql.util.ClassUtil;
import org.apache.calcite.adapter.enumerable.AggImplementor;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AggregateFunctionImpl;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.calcite.sql.SqlOperatorTable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * 抽象的函数来源
 */
public abstract class AbstractFuncSource {

    private static Logger log = LoggerFactory.getLogger(AbstractFuncSource.class);

    /**
     * 加载jar文件,由各个子类实现,这里的参数是一个不定参数.
     * 加载方法目前仅支持File和Maven.
     * eg:file 参数可以为  /home/finance/aa.jar (可选多个jar,但参数个数是1个,不同jar之间以逗号分隔见com.kanyun.ui.components.FunctionDialog#createJarTab())
     * eg:maven 参数为 "com.google.code.gson","gson","2.9.1" (只能填一个)
     *
     * @param args
     * @throws Exception
     */
    public abstract void loadJar(String... args) throws Exception;

    /**
     * 普通函数(UDF)集合,key为函数名,value为函数所在的类
     */
    private final static ConcurrentHashMap<String, Class> userDefineFunctions = new ConcurrentHashMap<>();

    /**
     * 内置函数(UDF)集合,key为函数名,value为函数所在的类
     */
    private final static ConcurrentHashMap<String, Class> innerDefineFunctions = new ConcurrentHashMap<>();

    /**
     * 内置聚合函数(UDAF),key为函数名,value为函数所在的类,聚合函数的方法名是大写的类名,聚合函数:一个类就是一个函数
     */
    private final static ConcurrentHashMap<String, Class> innerDefineAggregateFunctions = new ConcurrentHashMap<>();

    /**
     * 用户自定义聚合类函数(UDAF),区别：UDF：一行返回一行，UDAF：多行返回一行,如sum()/count()/avg() 注意：distinct()不是聚合函数,它仅仅去重
     * 用户定义的聚合函数类似于用户定义的函数，但是每个函数都有几个对应的Java方法，每个方法对应于聚合生命周期中的每个阶段:
     * init 创建一个累加器
     * add 将一行的值添加到累加器
     * merge 将两个累加器合二为一
     * result 完成累加器并将其转换为结果
     * 也就是说,如果要定义聚合函数,一定要在类中定义上述的几个方法,方法修饰符为public static
     */
    private final static ConcurrentHashMap<String, Class> userDefineAggregateFunctions = new ConcurrentHashMap<>();

    /**
     * 初始化加载内置函数
     */
    static {
        log.info("应用初始化:准备解析并缓存内置函数");
//        初始化反射工具类包,内置函数在包com.kanyun.sql.core.func下,同时定义子类型扫描器,扫描指定包下的Object的子类
//        如果要实现扫描指定类的子类则在filterResultsBy()方法中实现Predicate接口,判断参数是否与指定父类一致
        Reflections reflections = new Reflections("com.kanyun.sql.core.func", Scanners.SubTypes.filterResultsBy(c -> true));
        Set<Class<?>> classSet = reflections.getSubTypesOf(Object.class);
        for (Class<?> clazz : classSet) {
//            判断当前class不是枚举,接口,注解,抽象类等类型
            if (!clazz.isEnum() && !clazz.isInterface() && !clazz.isAnnotation()) {
                if (!Modifier.isAbstract(clazz.getModifiers())) {
                    cacheFunc(clazz, innerDefineFunctions, innerDefineAggregateFunctions);
                }
            }
        }
        log.info("内置函数加载完毕,自定义函数数量：[{}],自定义聚合函数数量:[{}]", innerDefineFunctions.size(), innerDefineAggregateFunctions.size());
    }


    /**
     * 解析jar包
     * 并取出需要的函数,这里仅取出 public static 修饰的方法
     *
     * @param jarFile
     * @param classLoader
     * @throws ClassNotFoundException
     */
    private void parseJar(JarFile jarFile, ClassLoader classLoader) throws ClassNotFoundException {
        log.info("准备解析jar文件:{}", jarFile.getName());
//        得到jar包中的元素,包括目录
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry jarEntry = entries.nextElement();
//            判断是否是目录
            if (jarEntry.isDirectory()) continue;
            String fullName = jarEntry.getName();
//            判断文件是否是class文件
            if (!fullName.endsWith(".class")) continue;
            String className = fullName.substring(0, fullName.length() - 6).replaceAll("/", ".");
            Class<?> clazz;
            if (classLoader == null) {
//                对于当前非jvm进程的类路径来说,不指定类加载器是会报错的:java.lang.ClassNotFoundException,因为默认的类加载器是AppClassLoader,它加载classPath下的jar
                clazz = Class.forName(className);
            } else {
                clazz = Class.forName(className, true, classLoader);
            }
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            cacheFunc(clazz, userDefineFunctions, userDefineAggregateFunctions);
        }
    }

    /**
     * 设置ClassPath
     * (注:)这里再设置 java.class.path 属性已经没有用了,因为AppClassLoader已经加载过了,不会再重新进行加载,除非重启jvm
     * 不过,虽然再次设置java.class.path 属性没用,但是我们可以获取AppClassLoader的实例,
     * 由于AppClassLoader是URLClassLoader的子类,因此AppClassLoader实例强可以强转为URLClassLoader,此时可以反射调用
     * URLClassLoader的addURL()方法,并传入jar路径,以实现动态jar的加载
     *
     * @param jarFiles
     */
    void setClassPath(List<File> jarFiles) {
//        AppClassLoader：用于加载CLASSPATH下的类,是大多数自定义类加载器的父加载器,因此该加载器也用于加载大多数的自定义类,其父加载器为ExtClassLoader,查找范围：java.class.path
        String classPath = System.getProperty("java.class.path");
//        获得当前函数包所在路径,多个函数包之前分号间隔
        String funcPath = jarFiles.stream().map(File::getAbsolutePath).collect(Collectors.joining(";"));
//        将jvm启动时的classpath与新添加的函数路径组合起来,并设置到classpath中
        classPath = classPath + ";" + funcPath;
        log.info("设置classPath的属性:{}", classPath);
//        此操作并不会触发类的加载
        System.setProperty("java.class.path", classPath);
//        关键在这一步,获取AppClassLoader示例并强转为URLClassLoader,然后反射调用URLClassLoader#addURL()实现AppClassLoader的动态加载
        jarFiles.stream().forEach(x -> ClassUtil.addClasspath(x.getAbsolutePath()));
    }

    /**
     * 解析并加载所有的Jar文件
     * 同一个类加载器(同一个类加载器实例)
     *
     * @param files
     * @throws Exception
     */
    void parseJar(List<File> files) throws Exception {
//        类加载器已经把所有jar的路径添加进去了
        ClassLoader classLoaderFromFile = createClassLoaderFromFile(files);
        setClassPath(files);
        for (File file : files) {
            JarFile jarFile = new JarFile(file);
            parseJar(jarFile, classLoaderFromFile);
        }
    }

    /**
     * 从文件URL获取类加载器
     * 一次加载所有的外部函数,使用同一个类加载器,而不是加载一个外部jar就创建一个类加载器
     *
     * @param files
     * @return
     * @throws MalformedURLException
     */
    protected ClassLoader createClassLoaderFromFile(List<File> files) throws MalformedURLException {
        URL[] urls = new URL[files.size()];
        for (int i = 0; i < files.size(); i++) {
            urls[i] = files.get(i).toURI().toURL();
        }
//        创建URLClassLoader类型的类加载器,第一个参数设置创建的类加载的父类加载器,不使用上下文类加载器  Thread.currentThread().getContextClassLoader()
//        URLClassLoader urlClassLoader =
//                URLClassLoader.newInstance(urls, ClassLoader.getSystemClassLoader());
        ExternalFuncClassLoader urlClassLoader = ExternalFuncClassLoader.newInstance(urls, ClassLoader.getSystemClassLoader());
        return urlClassLoader;
    }

    /**
     * 将函数缓存到容器中
     * Calcite自定义函数要求:函数方法可以是静态的，也可以是非静态的，但如果不是静态的，则类必须具有不带参数的公共构造函数
     * 这里先选择静态方法作为自定义函数进行缓存
     *
     * @param clazz
     * @param udfContainer
     * @param clazz
     */
    private static void cacheFunc(Class clazz, ConcurrentHashMap<String, Class> udfContainer, ConcurrentHashMap<String, Class> udafContainer) {
        if (isAggregationFunction(clazz)) {
//            聚合函数的方法名是类的大写
            udafContainer.put(clazz.getSimpleName().toUpperCase(), clazz);
            return;
        }
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
//                得到方法修饰符
            String modifier = Modifier.toString(method.getModifiers());
//                只有被public static 修饰的方法是我们用的方法
            if (modifier.contains("public") && modifier.contains("static")) {
                String msgTemplate = "类 [%s] 的方法 [%s] 被选中,将作为SQL函数使用";
                String msg = String.format(msgTemplate, clazz.getCanonicalName(), method.getName());
                log.info(msg);
                udfContainer.put(method.getName(), clazz);
            }
        }
    }

    /**
     * 判断当前类是否是聚合函数
     *
     * @return
     */
    private static boolean isAggregationFunction(Class<?> clazz) {
        boolean result = false;
        Method initMethod = ClassUtil.findMethod(clazz, "init");
        Method addMethod = ClassUtil.findMethod(clazz, "add");
        Method resultMethod = ClassUtil.findMethod(clazz, "result");
        if (initMethod != null && addMethod != null && resultMethod != null) {
            result = true;
        }
        return result;
    }

    /**
     * 动态注册函数
     */
    public static void registerFunction(SchemaPlus schemaPlus) {
        log.debug("=======Schema:[{}],开始注册函数======", schemaPlus.getParentSchema() == null ? "rootSchema" : schemaPlus.getName());
        Set<Map.Entry<String, Class>> udfEntries = userDefineFunctions.entrySet();
        udfEntries.addAll(innerDefineFunctions.entrySet());
        for (Map.Entry<String, Class> entry : udfEntries) {
            log.debug("待注册的函数信息：[{}.{}()]", entry.getValue().getName(), entry.getKey());
            try {
//                第一个参数为在SQL中使用的函数名,第二个参数是传入类及类的方法名所创建的函数实例.
//                例如:自定义的函数为com.kanyun.fun.CustomFunc#applyDate() 但是在写SQL时希望把函数名写为APPLY_DATE,此时第一个参数应为APPLY_DATE
                schemaPlus.add(entry.getKey(), ScalarFunctionImpl.create(entry.getValue(), entry.getKey()));
            } catch (Exception e) {
                log.error("自定义函数注册异常!:", e);
            }
        }
    }

    /**
     * 动态注册聚合函数
     * 动态注册自定义函数与动态注册普通函数方式不同
     * 1: 聚合函数是一个类表示一个函数
     * 2：聚合函数类中需要包含 init()/add()/result() 这三个静态方法 public static 修饰的方法
     * 3: 注册时需要创建AggregateFunction接口的实例 {@link  AggregateFunctionImpl#create(Class)}
     * 4: 创建AggregateFunction接口的实例的时候,需要实现自定义的AggImplementor接口的实例,该接口负责生成Linq4j的表达式 {@link  AggregateFunctionImpl#getImplementor(boolean)}
     * 5: 当 {@link  AggregateFunctionImpl#create(Class)} 不能满足需求时,可自定义实现AggregateFunction接口,并自定义实现{@link AggImplementor} 接口,难度较大,需要对linq表达式熟悉
     *
     * 目前发现的问题是,在使用自定义聚合函数时,需要注意聚合函数的参数类型,例如聚合函数的参数是Double类型,而字段类型是Integer,
     * 此时当给这个字段应用函数时,会报错(生成linq表达式报错,找不到类型对应的方法),
     * 解决思路：
     * 1.自定义AggregateFunction接口实现和AggImplementor实现,更改生成linq表达式逻辑
     * 2.sql中使用cast(字段 as 函数需要的类型)函数,先转换字段类型,在应用函数 custFunc(cast(字段 as custFunc期待的类型))
     */
    public static void registerAggFunction(SchemaPlus schemaPlus) {
        log.debug("=======Schema:[{}],开始注册聚合函数======", schemaPlus.getParentSchema() == null ? "rootSchema" : schemaPlus.getName());
        Set<Map.Entry<String, Class>> udafEntries = userDefineAggregateFunctions.entrySet();
        udafEntries.addAll(innerDefineAggregateFunctions.entrySet());
        for (Map.Entry<String, Class> entry : udafEntries) {
            log.debug("待注册的聚合函数信息：[{}.{}()]", entry.getValue().getName(), entry.getKey());
            try {
//                todo 动态注册自定义函数与动态注册普通函数方式不同,它需要创建AggregateFunction接口的实例
//                schemaPlus.add(entry.getKey(), DynamicAggFunctionImpl.create(entry.getValue(), entry.getKey()));
                schemaPlus.add(entry.getKey(), AggregateFunctionImpl.create(entry.getValue()));
            } catch (Exception e) {
                log.error("自定义聚合函数注册异常!:", e);
            }
        }

    }
}

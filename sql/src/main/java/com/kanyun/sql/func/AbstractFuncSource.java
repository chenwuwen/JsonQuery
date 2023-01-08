package com.kanyun.sql.func;

import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 抽象的函数来源
 */
public abstract class AbstractFuncSource {

    private static Logger log = LoggerFactory.getLogger(AbstractFuncSource.class);

    /**
     * 加载jar文件,由各个子类实现,这里的参数是一个不定参数.
     * 加载方法目前仅支持File和Maven.
     * eg:file 参数可以为  /home/finance/aa.jar
     * eg:maven 参数为 "com.google.code.gson","gson","2.9.1"
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
     * 解析jar包
     * 并取出需要的函数,这里仅取出 public static 修饰的方法
     * @param jarFile
     * @param classLoader
     * @throws ClassNotFoundException
     */
    void parseJar(JarFile jarFile, ClassLoader classLoader) throws ClassNotFoundException {
        log.info("得到jar文件,准备解析jar文件");
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
                clazz = Class.forName(className);
            } else {
                clazz = Class.forName(className, true, classLoader);
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
//                得到方法修饰符
                String modifier = Modifier.toString(method.getModifiers());
//                只有被public static 修饰的方法是我们用的方法
                if (modifier.contains("public") && modifier.contains("static")) {
                    String msgTemplate = "类 [%s] 的方法 [%s] 被选中";
                    String msg = String.format(msgTemplate, className, method.getName());
                    System.out.println(msg);
                    userDefineFunctions.put(method.getName(), clazz);
                }
            }

        }
    }

    /**
     * 注册函数
     */
    public static void registerFunction(SchemaPlus schemaPlus) {
        log.debug("=======开始注册函数======");
        Set<Map.Entry<String, Class>> entries = userDefineFunctions.entrySet();
        for (Map.Entry<String, Class> entry : entries) {
            log.debug("函数信息：[{}.{}]", entry.getValue(), entry.getKey());
            schemaPlus.add(entry.getKey(), ScalarFunctionImpl.create(entry.getValue(), entry.getKey()));
        }
    }
}

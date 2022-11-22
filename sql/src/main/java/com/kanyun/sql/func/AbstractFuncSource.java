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
     * 加载jar文件,由各个子类实现
     *
     * @param args
     * @throws Exception
     */
    public abstract void loadJar(String... args) throws Exception;

    /**
     * 所有函数集合,key为函数名,value为函数所在的类
     */
    private final static ConcurrentHashMap<String, Class> userDefineFunctions = new ConcurrentHashMap<>();

    /**
     * 解析jar包
     *
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

package com.kanyun.sql.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;

/**
 *
 */
public class ClassUtil {

    private static final Logger logger = LoggerFactory.getLogger(ClassUtil.class);

    /**
     * 添加jar到classpath中
     * 主要是获取类加载器并强转为URLClassLoader,并使用反射的形式调用其addURL()方法
     * 以实现动态添加jar
     *
     * @param path
     */
    public static void addClasspath(String path) {
        logger.info("添加Jar:{} 到 ClassPath", path);
        File file = new File(path);

        try {
            if (file.exists()) {
//                URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
//                获取线程上下文类加载器(一般是AppClassLoader),并转换为URLClassLoader
                URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
                Class<URLClassLoader> urlClass = URLClassLoader.class;
                Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
                method.setAccessible(true);
                method.invoke(urlClassLoader, new Object[]{file.toURI().toURL()});
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> Class<? extends T> forName(String name, Class<T> clz) throws ClassNotFoundException {
        return (Class<? extends T>) Class.forName(name);
    }

    public static <T> Constructor<? extends T> forConstructor(String name, Class... paramter) throws ClassNotFoundException, NoSuchMethodException {
        return (Constructor<? extends T>) Class.forName(name).getConstructor(paramter);
    }

    public static Object newInstance(String clz, Object paramter) {
        try {
            return forConstructor(clz, paramter.getClass()).newInstance(paramter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object newInstance(String clz) {
        try {
            return forName(clz, Object.class).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String findContainingJar(Class<?> clazz) {
        return findContainingJar(clazz, null);
    }

    /**
     * Load the first jar library contains clazz with preferJarKeyword matched. If preferJarKeyword is null, just load the
     * jar likes Hadoop Commons' ClassUtil
     *
     * @param clazz
     * @param preferJarKeyWord
     * @return
     */
    public static String findContainingJar(Class<?> clazz, String preferJarKeyWord) {
        ClassLoader loader = clazz.getClassLoader();
        String classFile = clazz.getName().replaceAll("\\.", "/") + ".class";

        try {
            Enumeration e = loader.getResources(classFile);

            URL url = null;
            do {
                if (!e.hasMoreElements()) {
                    if (url == null)
                        return null;
                    else
                        break;
                }

                url = (URL) e.nextElement();
                if (!"jar".equals(url.getProtocol()))
                    break;
                if (preferJarKeyWord != null && url.getPath().indexOf(preferJarKeyWord) != -1)
                    break;
                if (preferJarKeyWord == null)
                    break;
            } while (true);

            String toReturn = url.getPath();
            if (toReturn.startsWith("file:")) {
                toReturn = toReturn.substring("file:".length());
            }

            toReturn = URLDecoder.decode(toReturn, "UTF-8");
            return toReturn.replaceAll("!.*$", "");
        } catch (IOException var6) {
            throw new RuntimeException(var6);
        }
    }

    /**
     * 通过类查找方法
     * 1:判断类中是否存在methodName指定的方法
     * 2:判断查询到的方法是否是桥接方法(在Java中，如果一个接口在实现过程中，有多个方法签名不同的同名方法，那么在JDK 5.0之后，Java会自动生成一个桥接方法，该方法会将调用转发给具体实现的方法)
     * 3:判断方法是否是public修饰的方法
     * 4:判断方法是否是静态方法static
     *
     * @param clazz
     * @param methodName
     * @return
     */
    public static Method findMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName) && !method.isBridge() &&
                    Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {
                return method;
            }
        }
        return null;
    }
}

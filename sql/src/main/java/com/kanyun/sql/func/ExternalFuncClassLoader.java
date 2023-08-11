package com.kanyun.sql.func;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * 外部函数类加载器,单例模式。保证该类加载器在jvm中只存在一个实例
 * 此处配置类加载器主要是用来外部函数(非ClassPath中的jar)
 * 1.在应用初始化时使用该加载器加载配置的自定义函数的路径,解析其中的类和方法缓存并注册到Calcite中作为自定义函数使用
 * 2.将该类加载器的实例,设置到上下文类加载器中,供Calcite物理计划->Janino来编译运行SQL生成的Linq代码。由于SQL中
 * 使用了自定义函数,而该自定义函数并非AppClassLoader加载的,因此需要保证Janino能使用正确的类加载器,以便正确加载
 * 自定义函数类(目前还未实现)
 */
public class ExternalFuncClassLoader extends URLClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(ExternalFuncClassLoader.class);


    private static ExternalFuncClassLoader externalFuncClassLoader;

    /**
     * 私有的构造方法,禁止外部通过new方法创建类加载器实例
     *
     * @param urls
     * @param parent
     */
    private ExternalFuncClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }


    /**
     * 创建自定义类加载器实例
     *
     * @param urls
     * @param parent
     * @return
     */
    public static ExternalFuncClassLoader newInstance(URL[] urls, ClassLoader parent) {
        if (externalFuncClassLoader == null) {
//            如果传递的父加载器为null,则继承线程上下文类加载器
            externalFuncClassLoader = new ExternalFuncClassLoader(urls, parent == null ? Thread.currentThread().getContextClassLoader() : parent);

        }
        return externalFuncClassLoader;
    }

    /**
     * 添加jar
     *
     * @param files
     * @throws MalformedURLException
     */
    public void addJars(List<File> files) throws MalformedURLException {
        for (File file : files) {
            super.addURL(file.toURI().toURL());
        }
    }

    /**
     * 获取类加载器实例
     *
     * @return
     */
    public static ExternalFuncClassLoader getInstance() {
        if (externalFuncClassLoader == null) {
            URL[] urls = {};
            externalFuncClassLoader = new ExternalFuncClassLoader(urls, Thread.currentThread().getContextClassLoader());
            logger.warn("创建了未包含任何路径的自定义类加载器实例");
//            throw new NullPointerException();
        }
        return externalFuncClassLoader;
    }
}

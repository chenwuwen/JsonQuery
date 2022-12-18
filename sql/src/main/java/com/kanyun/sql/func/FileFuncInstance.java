package com.kanyun.sql.func;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

/**
 * 文件函数来源实例
 */
public class FileFuncInstance extends AbstractFuncSource{


    @Override
    public void loadJar(String... args) throws Exception {
        for (String arg : args) {
            File file = new File(arg);
            URL url = file.toURI().toURL();
            URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[]{url}, Thread.currentThread().getContextClassLoader());
            JarFile jarFile = new JarFile(file);
            parseJar(jarFile, urlClassLoader);
        }
    }
}

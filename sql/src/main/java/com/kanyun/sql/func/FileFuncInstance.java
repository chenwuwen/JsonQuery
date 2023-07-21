package com.kanyun.sql.func;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

/**
 * 文件函数来源实例
 * jar来源的函数,可能包含多个jar,多个jar之间分隔符{@link com.kanyun.ui.model.Constant.FUNC_JAR_FILE_SEPARATOR}
 */
public class FileFuncInstance extends AbstractFuncSource {


    @Override
    public void loadJar(String... args) throws Exception {
        List<File> files = new ArrayList<>();
        String paths = args[0];
        for (String path : paths.split(",")) {
            File file = new File(path);
            files.add(file);
        }
        parseJar(files);
    }
}

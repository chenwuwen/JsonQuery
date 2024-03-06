package com.kanyun.ui.splash;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 解析Svg工具类
 */
public class SvgAnalysisHelper {

    private static Logger logger = LoggerFactory.getLogger(SvgAnalysisHelper.class);

    /**
     * 创建 DocumentBuilder 实例的工厂类
     */
    private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    /**
     * 用于解析 XML 文档，当你想要从 XML字符串或文件中读取并解析 XML 数据时，
     * 你可以使用 DocumentBuilderFactory 来获取一个 DocumentBuilder，
     * 然后使用这个 DocumentBuilder 来创建一个 Document 对象，该对象代表了整个 XML 文档的内存表示。
     */
    private static DocumentBuilder builder = null;


    /**
     * 获取SVG文件的path
     *
     * @param svgPath
     * @return
     */
    public static List<String> getSvgPath(String svgPath) {
        List<String> pathList = new ArrayList<>();
        try {
            instanceDocumentBuilder();
            Document document = null;
//            判读svg路径是否在Jar包中
            if (svgPath.contains(".jar!")) {
                logger.info("准备从Jar包中获取logo的图像流");
                document = builder.parse(getSvgInputStream(svgPath));
            } else {
                document = builder.parse(svgPath);
            }
            logger.info("已从svg中读取到内容,并实例化Document对象");
            document.getDocumentElement().normalize();
            // 获取SVG路径
            NodeList nodeList = document.getElementsByTagName("path");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String path = element.getAttribute("d");
                    pathList.add(path);
                }
            }
            return pathList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取SVG图像的尺寸
     *
     * @param svgPath
     * @return
     */
    public static Pair<Double, Double> getSvgSize(String svgPath) {
        try {
            instanceDocumentBuilder();
            Document document = null;
//            判读svg路径是否在Jar包中
            if (svgPath.contains(".jar!")) {
                logger.info("准备从Jar包中获取logo的图像流");
                document = builder.parse(getSvgInputStream(svgPath));
            } else {
                document = builder.parse(svgPath);
            }
            document.getDocumentElement().normalize();
            Element documentElement = document.getDocumentElement();
//            获取svg属性viewBox
            String viewBox = documentElement.getAttribute("viewBox");
            String[] point = viewBox.split(" ");
            double x = Double.parseDouble(point[0]);
            double y = Double.parseDouble(point[1]);
            double width = Double.parseDouble(point[2]);
            double height = Double.parseDouble(point[3]);
            return Pair.of(width, height);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从Jar包中获取svg的输入流
     *
     * @param svgPath Jar包内的路径
     * @return
     */
    private static InputStream getSvgInputStream(String svgPath) {
        int index = svgPath.indexOf("!");
        String jarPath = svgPath.substring(6, index);
        logger.info("解析后Jar路径：{}", jarPath);
        try {
            String svg = svgPath.substring(index + 2);
            logger.info("svg在Jar中的相对路径：{}", svg);
            JarFile jarFile = new JarFile(jarPath);
            System.out.println(jarFile.size());
            JarEntry svgEntry = jarFile.getJarEntry(svg);
            InputStream inputStream = jarFile.getInputStream(svgEntry);
            return inputStream;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static DocumentBuilder instanceDocumentBuilder() {
        if (builder == null) {
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
        return builder;
    }
}

package com.kanyun.ui.splash;

import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析Svg工具类
 */
public class SvgAnalysisHelper {

    private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder builder = null;


    /**
     * 获取SVG文件的path
     * @param svgPath
     * @return
     */
    public static List<String> getSvgPath(String svgPath) {
        List<String> pathList = new ArrayList<>();
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(svgPath);
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
     * @param svgPath
     * @return
     */
    public static Pair<Double,Double> getSvgSize(String svgPath) {
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(svgPath);
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
}

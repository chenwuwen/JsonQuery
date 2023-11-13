package com.kanyun.ui;

import com.jfoenix.svg.SVGGlyph;
import com.jfoenix.svg.SVGGlyphLoader;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Icon属性设置,实现ICON的统一管理,resource下创建icon.properties文件
 * 设置好key-value,UI界面如需使用icon,则调用 {@link IconProperties#getIconName(String)} 传递icon的key值
 * 获取到图标名
 */
public class IconProperties {
    private static final Logger logger = LoggerFactory.getLogger(IconProperties.class);

    private static final Properties properties = new Properties();

    static {
        InputStream iconPropInputStream = IconProperties.class.getClassLoader().getResourceAsStream("icon.properties");
        try {
            properties.load(iconPropInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取图标名前缀
     *
     * @return
     */
    public static String getIconNamePrefix() {
        return String.valueOf(properties.get("icon.prefix"));
    }

    /**
     * 获取图标名前缀
     *
     * @return
     */
    public static String getIconName(String iconNameKey) {
        return getIconNamePrefix() + "." + properties.get(iconNameKey);
    }

    /**
     * 获取图标
     * 需要注意的是,当自己想要的图标获取为空时,最好看看图标名是否正确,因为图标名可能包含空格或其他符号
     * 这里使用JFoenix的SVGGlyphLoader来加载图标,可以加载多个字体,不同字体的同名图标,可以在加载字体时
     * 设置前缀,这里在传递图标名称时,也需要带上图标前缀
     *
     * @param iconNameKey 图标的key 见 icon.properties
     * @param width       图标宽度
     * @param height      图标高度
     * @return
     */
    public static Node getIcon(String iconNameKey, double width, double height, Color color) {
//        获取图标名称 注意图标名称可能包含空格等特殊符号,当无法获取图标时,请再三验证图标名称是否正确
        String glyphName = getIconName(iconNameKey);
        logger.debug("创建图标:{}", glyphName);
        SVGGlyph svgGlyph = SVGGlyphLoader.getGlyph(glyphName);
        svgGlyph.setFill(color);
        svgGlyph.setSize(width, height);
        return svgGlyph;
    }

    /**
     * 获取图标
     *
     * @param iconNameKey
     * @param iconSize    图标尺寸,长宽一致
     * @return
     */
    public static Node getIcon(String iconNameKey, double iconSize, Color color) {
        return getIcon(iconNameKey, iconSize, iconSize, color);
    }

    /**
     * 获取ImageView图标
     * @param imgPath
     * @param iconSize
     * @return
     */
    public static Node getImageView(String imgPath, double iconSize) {
        ImageView tableImageView = new ImageView(imgPath);
        tableImageView.setFitHeight(iconSize);
        tableImageView.setFitWidth(iconSize);
        return tableImageView;
    }
}

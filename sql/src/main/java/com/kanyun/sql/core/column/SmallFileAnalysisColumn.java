package com.kanyun.sql.core.column;

import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 小文件字段解析,直接加载json文件进行解析
 */
public class SmallFileAnalysisColumn extends AbstractAnalysisJsonTableColumn {
    private static final Logger log = LoggerFactory.getLogger(SmallFileAnalysisColumn.class);

    protected SmallFileAnalysisColumn(File file, String tableName, String schemaName) {
        super(file, tableName, schemaName);
    }


    @Override
    boolean checkJsonFileIsStand() throws Exception {
        try {
//			  加载json文件
            String content = Files.asCharSource(file, StandardCharsets.UTF_8).read();
//            将json文件转换为JsonElement对象
            JsonElement jsonElement = JsonParser.parseString(content);
//            需要保证Json文件内容是JsonArray类型
            return jsonElement.isJsonArray() && jsonElement.getAsJsonArray().get(0).isJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    List<JsonTableColumn> analysisTableField() throws Exception {

//        加载json文件
        String content = Files.asCharSource(file, StandardCharsets.UTF_8).read();
//       取出Json文件并转换成数组,然后取出数组中的第一个元素(JsonObject)
        JsonObject jsonObject = JsonParser.parseString(content).getAsJsonArray().get(0).getAsJsonObject();
        return analysisJsonItem(jsonObject);
    }
}

package com.kanyun.sql.core.column;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 大文件字段解析,采用流方式解析字段,节省内存
 */
public class LargeFileAnalysisColumn extends AbstractAnalysisJsonTableColumn {

    private static final Logger log = LoggerFactory.getLogger(LargeFileAnalysisColumn.class);

    protected LargeFileAnalysisColumn(File file, String tableName, String schemaName) {
        super(file, tableName, schemaName);
    }


    @Override
    boolean checkJsonFileIsStand() throws Exception {
        JsonReader jsonReader = new JsonReader(new FileReader(file));
        try {
            jsonReader.beginArray();
            jsonReader.beginObject();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    List<JsonTableColumn> analysisTableField() throws Exception {
        List<JsonTableColumn> jsonTableColumnList = new ArrayList<>();
        JsonReader jsonReader = new JsonReader(new FileReader(file));
//        开始于数组([)
        jsonReader.beginArray();
//        数组之后开始于对象({)
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            JsonTableColumn jsonTableColumn = new JsonTableColumn();
//            获得对象中的某个Key
            String key = jsonReader.nextName();
            jsonTableColumn.setName(key);
//            只获取下一个Token并不消费,使用nextString()/nextBoolean()等方法时消费
            JsonToken token = jsonReader.peek();
            if (token == JsonToken.STRING) {
//                消费Token
                String value = jsonReader.nextString();
                jsonTableColumn.setType(ColumnType.VARCHAR);
                log.debug("[{}.{}]-> 解析字段类型,字段:{} , 类型:{},值：{}", schemaName, tableName, key, TYPE_VARCHAR, value);
            }
            if (token == JsonToken.BOOLEAN) {
//                消费Token
                boolean value = jsonReader.nextBoolean();
                jsonTableColumn.setType(ColumnType.BOOLEAN);
                log.debug("[{}.{}]->解析字段类型,字段:{} , 类型:{},值：{}", schemaName, tableName, key, TYPE_BOOLEAN, value);
            }
            if (token == JsonToken.NUMBER) {
//                消费Token,因为Double类型可以表示的区间更大,因此使用nextDouble()方法更稳妥
                Number number = jsonReader.nextDouble();
                analysisColumnTypeForNumber(number, jsonTableColumn);
                log.debug("[{}.{}]->解析字段类型,字段:{} , 类型:{},值：{}", schemaName, tableName, key, jsonTableColumn.getType(), number);
            }
            if (token == JsonToken.BEGIN_ARRAY || token == JsonToken.BEGIN_OBJECT) {
                jsonTableColumn.setType(ColumnType.VARCHAR);
                String value = jsonReader.nextString();
                log.debug("[{}.{}]->解析字段类型,字段:{} ,值：{},由于值类型为Object/Array因此设置类型为:{}", schemaName, tableName, key, value, TYPE_VARCHAR);
            }
            jsonReader.skipValue();
            jsonTableColumnList.add(jsonTableColumn);
        }
        jsonReader.endObject();
        jsonReader.close();
        return jsonTableColumnList;
    }

}

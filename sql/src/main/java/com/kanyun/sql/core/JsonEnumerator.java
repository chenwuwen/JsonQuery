package com.kanyun.sql.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.Linq4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 元素迭代器,定义数据的输出
 */
public class JsonEnumerator implements Enumerator<Object[]> {

    private static final Logger log = LoggerFactory.getLogger(JsonEnumerator.class);

    private Enumerator<LinkedHashMap<String, Object>> enumerator;


    public JsonEnumerator(File file) {
        ObjectMapper mapper = new ObjectMapper();
//        parser是否将允许使用非双引号属性名字(这种形式在Javascript中被允许，但是JSON标准说明书中没有)注意：由于JSON标准上需要为属性名称使用双引号，所以这也是一个非标准特性，默认是false的
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
//        parser是否允许单引号来包住属性名称和字符串值
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
//        parser将是否允许解析使用Java/C++ 样式的注释(包括'/'+'*' 和'//' 变量)由于JSON标准说明书上面没有提到注释是否是合法的组成,所以这是一个非标准的特性;尽管如此,这个特性还是被广泛地使用
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
//        这个特性，决定了解析器是否将自动关闭那些不属于parser自己的输入源,如果禁止则调用应用不得不分别去关闭那些被用来创建parser的基础输入流InputStream和reader,如果允许，parser只要自己需要获取closed方法(当遇到输入流结束，或者parser自己调用 JsonParser#close方法)
        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
//        如果是空对象的时候,不抛异常
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//        反序列化时,遇到未知属性会不会报错
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


        List<LinkedHashMap<String, Object>> tableData;
        try {
            tableData = mapper.readValue(file, new TypeReference<List<LinkedHashMap<String, Object>>>() {

            });
            log.debug("records count [{}]", tableData.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        enumerator = Linq4j.enumerator(tableData);
    }

    @Override
    public Object[] current() {
        LinkedHashMap<String, Object> current = enumerator.current();
        return current.values().toArray();
    }

    @Override
    public boolean moveNext() {
        return enumerator.moveNext();
    }

    @Override
    public void reset() {
        enumerator.reset();
    }

    @Override
    public void close() {
        enumerator.close();
    }

}

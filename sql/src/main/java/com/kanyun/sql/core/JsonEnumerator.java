package com.kanyun.sql.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.kanyun.sql.core.column.JsonTableColumn;
import com.kanyun.sql.core.column.JsonTableColumnFactory;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.Linq4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 元素迭代器,定义数据的输出
 * Linq4j(Linq) 是真正的执行查询数据源的操作
 * Linq是一个支持查询各种数据源的框架
 * LINQ（语言集成查询）是C＃和VB.NET中的统一查询语法，用于从不同的源和格式检索数据。它集成在C＃或VB中，
 * 从而消除了编程语言和数据库之间的不匹配，并为不同类型的数据源提供了单个查询接口:https://www.cainiaojc.com/linq/what-is-linq.html
 * LINQ4j:是Java语言实现的一个开源的LINQ
 * Calcite通过物理执行计划生成,最终得到完整的LINQ的BlockStatement,也就是完整的LINQ代码,最后通过Janino编译执行
 */
public class JsonEnumerator implements Enumerator<Object[]> {

    private static final Logger log = LoggerFactory.getLogger(JsonEnumerator.class);

    private Enumerator<LinkedHashMap<String, Object>> enumerator;
    /**
     * 当前Schema
     */
    private String schema;
    /**
     * 当前表
     */
    private String table;


    public JsonEnumerator(File file, String schema, String table) {
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
            log.error("反序列化文件:{}失败", file.getAbsolutePath(), e);
            throw new RuntimeException("反序列化" + file.getAbsolutePath() + "失败", e);
        }
//        将元素转换为Linq的Enumerable对象
        enumerator = Linq4j.enumerator(tableData);
        this.schema = schema;
        this.table = table;
    }

    @Override
    public Object[] current() {
//        取当前行(Json数据),并反序列化。即：ResultSet,由于json文件的不确定性 current.size()可能不同
//        可能存在如下json: [{"name":"看云","sex":"男","age":30},{"name","无问","sex":"男"},{"name","天明"}]
        LinkedHashMap<String, Object> current = enumerator.current();
//        修复后的行数据
        Map<String, Object> fixRowData = fixJsonConfusion(current);
        return fixRowData.values().toArray();
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

    /**
     * 修复json文件错乱,并返回修复后的数据
     * 可能存在如下json: [{"name":"看云","sex":"男","age":30},{"name","无问","sex":"男"},{"name","天明"}]
     *
     * @param currentRowData 从文件中读取到的当前行数据库
     */
    private Map<String, Object> fixJsonConfusion(LinkedHashMap<String, Object> currentRowData) {
//        获取数据库表的元数据信息(即：字段信息),这里jsonFile参数传递null,是因为此时一定是已经解析过(或维护过)字段信息了,因此可以传空
        List<JsonTableColumn> tableColumnInfoList = JsonTableColumnFactory.getTableColumnInfoList(null, schema, table);
//        修复后的行数据
        Map<String, Object> fixRowData = new LinkedHashMap<>();
        for (JsonTableColumn jsonTableColumn : tableColumnInfoList) {
            if (currentRowData.containsKey(jsonTableColumn.getName())) {
                fixRowData.put(jsonTableColumn.getName(), currentRowData.get(jsonTableColumn.getName()));
            } else {
//               todo json元素中未包含的字段先赋值为NULL,也可以为每个字段配置默认值,在此处设置字段配置的默认值
                fixRowData.put(jsonTableColumn.getName(), null);
            }
        }
        return fixRowData;
    }
}

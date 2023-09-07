package com.kanyun.sql.core.column;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.kanyun.sql.util.H2Utils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 数据库表字段工厂类
 * 用于获取表的元数据信息(即:字段信息) 这一点非常重要
 * 理由如下：
 * JsonQuery虽然可以通过解析Json文件(通过读取json文件的第一个json子元素)生成表的字段信息,但是由于json文件的不确定性
 * 如:[{"name","无问","sex":"男"},{"name":"看云","sex":"男","age":30},{"name","天明"}]
 * 这就导致了自动获取的表字段信息存在少字段的情况,虽然对于上述json文件 第三个 子元素 也无法获取其 age属性
 * 但少字段的情况其实更加严重,因此手动维护表字段是非常有必要的。JsonQuery也提供了手动维护字符的UI处理
 * 获取字段信息时优先获取手动维护的字段信息 {@link JsonTableFieldCacheCallback#call()}
 */
public class JsonTableColumnFactory {

    private static final Logger log = LoggerFactory.getLogger(JsonTableColumnFactory.class);

    /**
     * 数据库表字段缓存
     */
    private static Cache<String, List<JsonTableColumn>> TABLE_FIELD_CACHE = CacheBuilder
            .newBuilder()
            .maximumSize(200)
            .recordStats()
            .expireAfterWrite(120, TimeUnit.MINUTES)
            .build();


    /**
     * 文件最大字节：5M
     * Json数据文件小于5m,则加载Json文件全部数据用于解析字段信息
     * 大于5m,采用流式解析Json文件获取字段信息
     */
    private static Integer MAX_LENGTH = 5 * 1024 * 1024;

    /**
     * 获取表的字段信息
     * 先从缓存获取(根据schemaName，tableName组成的Key)查询
     * 查询不到,走JsonTableFieldCacheCallback类的call方法查询
     *
     * @param jsonFile   Json数据文件
     * @param schemaName schema名称
     * @param tableName  表名称
     * @return
     */
    public static List<JsonTableColumn> getTableColumnInfoList(File jsonFile, String schemaName, String tableName) {
        String cacheKey = schemaName + "." + tableName;
        try {
            return TABLE_FIELD_CACHE.get(cacheKey, new JsonTableFieldCacheCallback(jsonFile, schemaName, tableName));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 刷新表字段信息缓存
     *
     * @param schemaName
     * @param tableName
     * @param jsonTableColumnList
     */
    public static void refreshTableColumnInfo(String schemaName, String tableName, List<JsonTableColumn> jsonTableColumnList) {
        String cacheKey = schemaName + "." + tableName;
        TABLE_FIELD_CACHE.put(cacheKey, jsonTableColumnList);
        String deleteSql = "delete from `field_info` where `schema`='" + schemaName + "' and `table`='" + tableName + "'";
        try {
            H2Utils.executeSql(deleteSql);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        addTableColumnInfo(schemaName, tableName, jsonTableColumnList);
    }

    /**
     * 添加表的字段信息
     *
     * @param schemaName
     * @param tableName
     * @param jsonTableColumnList
     * @throws SQLException
     */
    public static void addTableColumnInfo(String schemaName, String tableName, List<JsonTableColumn> jsonTableColumnList) {
        String insertSql = "insert into `field_info` (`schema`,`table`,`name`,`type`,`default_value`) values";
        StringJoiner valuesJoiner = new StringJoiner(",");
        String valueTmp = "('%s','%s','%s','%s','%s')";
        for (JsonTableColumn jsonTableColumn : jsonTableColumnList) {
            String value = String.format(valueTmp, schemaName, tableName,
                    jsonTableColumn.getName(), jsonTableColumn.getType().toCode(),
                    jsonTableColumn.getDefaultValue());
            valuesJoiner.add(value);
        }
//        拼接SQL
        insertSql = insertSql + valuesJoiner;
        try {
            H2Utils.executeSql(insertSql);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }


    /**
     * 加载所有的表字段信息
     */
    public static void loadTableColumnInfo() throws SQLException {
        StopWatch stopWatch = StopWatch.createStarted();
        TABLE_FIELD_CACHE.invalidateAll();
        ResultSet resultSet = H2Utils.execQuery("select * from `field_info` ");
        Map<String, List<JsonTableColumn>> cache = new HashMap<>();
        while (resultSet.next()) {
            String cacheKey = resultSet.getString("schema") + "." + resultSet.getString("table");
            List<JsonTableColumn> columnInfos = cache.getOrDefault(cacheKey, new ArrayList<>());
            JsonTableColumn jsonTableColumn = new JsonTableColumn();
            jsonTableColumn.setName(resultSet.getString("name"));
            jsonTableColumn.setType(ColumnType.getColumnTypeByCode(resultSet.getString("type")));
            jsonTableColumn.setDefaultValue(resultSet.getString("default_value"));
            columnInfos.add(jsonTableColumn);
        }
        resultSet.close();
        TABLE_FIELD_CACHE.putAll(cache);
        stopWatch.stop();
        log.info("加载所有表元数据信息完毕,耗时:{}ms", stopWatch.getTime());
    }

    /**
     * 从数据库中获取表字段信息
     *
     * @param schema
     * @param table
     * @return
     */
    private static List<JsonTableColumn> getJsonTableColumn(String schema, String table) {
        String querySql = "select * from `field_info` where `schema`='%s' and `table`='%s'";
        List<JsonTableColumn> jsonTableColumnList = new ArrayList<>();
        try {
            ResultSet resultSet = H2Utils.execQuery(String.format(querySql, schema, table));

            while (resultSet.next()) {
                JsonTableColumn jsonTableColumn = new JsonTableColumn();
                String type = resultSet.getString("type");
                jsonTableColumn.setType(ColumnType.getColumnTypeByCode(type));
                jsonTableColumn.setName(resultSet.getString("name"));
                jsonTableColumn.setDefaultValue(resultSet.getString("default_value"));
                jsonTableColumnList.add(jsonTableColumn);
            }
            resultSet.close();
            return jsonTableColumnList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonTableColumnList;
    }


    /**
     * Cache取不到数据回调类,先查询数据库,数据库没有则解析文件
     * 解析完文件,将字段信息存储到数据库中
     */
    static class JsonTableFieldCacheCallback implements Callable<List<JsonTableColumn>> {

        private File jsonFile;
        private String schemaName;
        private String tableName;

        public JsonTableFieldCacheCallback(File jsonFile, String schemaName, String tableName) {
            this.jsonFile = jsonFile;
            this.schemaName = schemaName;
            this.tableName = tableName;
        }

        @Override
        public List<JsonTableColumn> call() throws Exception {
//            todo 先通过 schemaName和tableName 查询数据库或者配置文件获取字段信息,非常重要因为配置文件或数据库中可能保存了手动维护的字段信息,这往往比解析文件获取的字段信息准确的多
            List<JsonTableColumn> jsonTableColumn = getJsonTableColumn(schemaName, tableName);
            if (jsonTableColumn.size() != 0) return jsonTableColumn;
            long length = jsonFile.length();
            AbstractAnalysisJsonTableColumn analysisJsonTableColumn;
            if (length <= MAX_LENGTH) {
                analysisJsonTableColumn = new SmallFileAnalysisColumn(jsonFile, schemaName, tableName);
            } else {
                analysisJsonTableColumn = new LargeFileAnalysisColumn(jsonFile, schemaName, tableName);
            }
            List<JsonTableColumn> tableColumnFromJsonFile = null;
            try {
//                解析文件获取字段信息
                tableColumnFromJsonFile = analysisJsonTableColumn.getTableColumnFromJsonFile();
                addTableColumnInfo(schemaName, tableName, tableColumnFromJsonFile);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Schema:[{}],文件:[{}],获取表字段异常", schemaName, jsonFile.getName(), e);
            }
            return tableColumnFromJsonFile;
        }
    }

}

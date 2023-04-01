package com.kanyun.sql.core.column;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 数据库表字段工厂类
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
//            todo 可以先通过 schemaName和tableName 查询数据库或者配置文件获取字段信息
            long length = jsonFile.length();
            AbstractAnalysisJsonTableColumn analysisJsonTableColumn;
            if (length <= MAX_LENGTH) {
                analysisJsonTableColumn = new SmallFileAnalysisColumn(jsonFile, schemaName, tableName);
            } else {
                analysisJsonTableColumn = new LargeFileAnalysisColumn(jsonFile, schemaName, tableName);
            }
            List<JsonTableColumn> tableColumnFromJsonFile = null;
            try {
                tableColumnFromJsonFile = analysisJsonTableColumn.getTableColumnFromJsonFile();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Schema:[{}],文件:[{}],获取表字段异常", schemaName, jsonFile.getName(), e);
            }
            return tableColumnFromJsonFile;
        }
    }

}

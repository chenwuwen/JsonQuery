package com.kanyun.ui.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 导出Excel转换类
 */
public class ExportExcelConvert {

    /**
     * TableView的字段信息
     */
    private List<String> columns;
    /**
     * TableView的行数据
     */
    private List<Map<String, Object>> rows;

    /**
     * 阿里EasyExcel框架需要写出数据的表内容
     */
    private List<List<Object>> exportExcelData = new LinkedList<>();

    /**
     * 阿里EasyExcel框架需要写出数据的表头
     * EasyExcel 需要的表头格式是 List<List<String>> 类型 子类型 List<String> 即每个字段用一个List<String>类型来表示
     */
    private List<List<String>> exportExcelHeaders = new LinkedList<>();

    public ExportExcelConvert(List<String> columns, List<Map<String, Object>> rows) {
        this.columns = columns;
        this.rows = rows;
        convert();
    }

    /**
     * 转换方法
     */
    private void convert() {
//        转换行数据
        for (Map<String, Object> row : rows) {
            List<Object> item = new LinkedList<>();
            for (String column : columns) {
                Object o = row.get(column);
                item.add(o);
            }
            exportExcelData.add(item);
        }
//        转换表头数据
        exportExcelHeaders = columns.stream().map(Collections::singletonList).collect(Collectors.toList());
    }

    public List<List<Object>> getExportExcelData() {
        return exportExcelData;
    }

    public List<List<String>> getExportExcelHeaders() {
        return exportExcelHeaders;
    }
}

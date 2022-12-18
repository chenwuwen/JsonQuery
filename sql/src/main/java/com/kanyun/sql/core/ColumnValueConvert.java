package com.kanyun.sql.core;

import org.apache.commons.lang3.tuple.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ColumnValueConvert {
    /**
     * 原始列信息(字段名,与取字段值的方法)
     * key为字段名,value为元组类型,元组类型的左值为字段名对应值的类型如java.lang.String
     * 元组类型的右值是一个取对应字段值的方法(Function类型)
     */
    private Map<String, Pair<Class, Function>> convertColumnInfos = new HashMap<>();

    /**
     * 构造方式,参数是Map类型,其中key为字段名称,value为calcite设定的字段类型(以数字表示)
     * 该构造方法,将字段名与取字符值的方法进行封装
     * @param sourceColumnInfos
     */
    public ColumnValueConvert(Map<String, Integer> sourceColumnInfos) {
        for (Map.Entry<String, Integer> entry : sourceColumnInfos.entrySet()) {
            switch (entry.getValue()) {
                case Types.VARCHAR:
                    convertColumnInfos.put(entry.getKey(), getPairForString(entry.getKey()));
                    break;
                case Types.FLOAT:
                    convertColumnInfos.put(entry.getKey(), getPairForFloat(entry.getKey()));
                    break;
                case Types.DATE:
                    convertColumnInfos.put(entry.getKey(), getPairForDate(entry.getKey()));
                    break;
                case Types.INTEGER:
                    convertColumnInfos.put(entry.getKey(), getPairForInteger(entry.getKey()));
                    break;
                case Types.BIGINT:
                    convertColumnInfos.put(entry.getKey(), getPairForLong(entry.getKey()));
                    break;
            }
        }
    }

    /**
     * 抽取行数据
     * @param rs
     * @return
     */
    public Map<String, Object> extractRowData(ResultSet rs) {
        HashMap<String, Object> rowData = new HashMap<>();
        for (Map.Entry<String, Pair<Class, Function>> entry : convertColumnInfos.entrySet()) {
//            取到元组的右值,是一个Function类型
            Function func = entry.getValue().getRight();
//            调用Function的apply方法,取出字段对应的值
            rowData.put(entry.getKey(), func.apply(rs));
        }
        return rowData;
    }


    /**
     * 取字符型字段值的方法
     * @param columnLabel 字段名(或as后的别名)
     * @return
     */
    public Pair getPairForString(String columnLabel) {
        Pair<Class, Function<ResultSet, String>> pair = Pair.of(java.lang.String.class, new Function<ResultSet, String>() {
            @Override
            public String apply(ResultSet resultSet) {
                try {
                    return resultSet.getString(columnLabel);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
                return null;
            }
        });
        return pair;
    }

    /**
     * 取浮点型字段值的方法
     * @param columnLabel 字段名(或as后的别名)
     * @return
     */
    public Pair getPairForFloat(String columnLabel) {
        Pair<Class, Function<ResultSet, Float>> pair = Pair.of(java.lang.Float.class, new Function<ResultSet, Float>() {
            @Override
            public Float apply(ResultSet resultSet) {
                try {
                    return resultSet.getFloat(columnLabel);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
                return null;
            }
        });
        return pair;
    }

    /**
     * 取整形字段值的方法
     * @param columnLabel 字段名(或as后的别名)
     * @return
     */
    public Pair getPairForInteger(String columnLabel) {
        Pair<Class, Function<ResultSet, Integer>> pair = Pair.of(java.lang.Integer.class, new Function<ResultSet, Integer>() {
            @Override
            public Integer apply(ResultSet resultSet) {
                try {
                    return resultSet.getInt(columnLabel);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
                return null;
            }
        });
        return pair;
    }

    /**
     * 取日期型字段值的方法
     * @param columnLabel 字段名(或as后的别名)
     * @return
     */
    public Pair getPairForDate(String columnLabel) {
        Pair<Class, Function<ResultSet, Date>> pair = Pair.of(java.util.Date.class, new Function<ResultSet, Date>() {
            @Override
            public Date apply(ResultSet resultSet) {
                try {
                    return resultSet.getDate(columnLabel);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
                return null;
            }
        });
        return pair;
    }

    /**
     * 取长整型字段值的方法
     * @param columnLabel 字段名(或as后的别名)
     * @return
     */
    public Pair getPairForLong(String columnLabel) {
        Pair<Class, Function<ResultSet, Long>> pair = Pair.of(java.lang.Long.class, new Function<ResultSet, Long>() {
            @Override
            public Long apply(ResultSet resultSet) {
                try {
//                   这里没有直接使用 resultSet.getLong() 是因为当得到的值比较小值,会返回Integer类型的值,然后会报类型转换异常
                    return Long.parseLong(resultSet.getString(columnLabel));
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
                return null;
            }
        });
        return pair;
    }
}

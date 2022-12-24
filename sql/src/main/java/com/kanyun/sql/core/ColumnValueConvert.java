package com.kanyun.sql.core;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 字段值类型转换类,字段与字段类型映射关系,由Calcite查询后得到
 */
public class ColumnValueConvert {

    private static final Logger log = LoggerFactory.getLogger(ColumnValueConvert.class);

    /**
     * 错误信息模板
     */
    private static final String TYPE_CONVERT_ERROR_MESSAGE_TEMPLATE = "字段:[%s] 类型转换异常,期待类型:[%s],输入值:[%s],扩展异常:[%s]";


    /**
     * 原始列信息(字段名,与取字段值的方法)
     * key为字段名,value为元组类型,元组类型的左值为字段名对应值的类型如java.lang.String
     * 元组类型的右值是一个取对应字段值的方法(GetColumnValueFunction类型)
     */
    private Map<String, Pair<Class, GetColumnValueFunction>> convertColumnInfos = new HashMap<>();

    /**
     * 构造方式,参数是Map类型,其中key为字段名称,value为calcite设定的字段类型(以数字表示)
     * 该构造方法,将字段名与取字符值的方法进行封装
     *
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
                case Types.DOUBLE:
                    convertColumnInfos.put(entry.getKey(), getPairForDouble(entry.getKey()));
                    break;
                case Types.BIGINT:
                    convertColumnInfos.put(entry.getKey(), getPairForLong(entry.getKey()));
                    break;
                case Types.BOOLEAN:
                    convertColumnInfos.put(entry.getKey(), getPairForBoolean(entry.getKey()));
                    break;
                default:
                    convertColumnInfos.put(entry.getKey(), getPairForUnknownType(entry.getKey()));
            }
        }
    }


    /**
     * 抽取行数据
     *
     * @param rs 行数据
     * @return
     */
    public Map<String, Object> extractRowData(ResultSet rs) throws Exception {
        HashMap<String, Object> rowData = new HashMap<>();
        for (Map.Entry<String, Pair<Class, GetColumnValueFunction>> entry : convertColumnInfos.entrySet()) {
//            取到元组的右值,是一个GetColumnValueFunction类型
            GetColumnValueFunction<ResultSet, Object> func = entry.getValue().getRight();
//            调用GetColumnValueFunction的apply方法,取出字段对应的值
            rowData.put(entry.getKey(), func.apply(rs));
        }
        return rowData;
    }


    /**
     * 取字符型字段值的方法
     *
     * @param columnLabel 字段名(或as后的别名)
     * @return
     */
    public Pair getPairForString(String columnLabel) {
        Pair<Class, GetColumnValueFunction<ResultSet, String>> pair = Pair.of(java.lang.String.class, new GetColumnValueFunction<ResultSet, String>() {
            @Override
            public String apply(ResultSet resultSet) throws Exception {
                try {
                    if (columnLabel.contains("(")){
                        int index = columnLabel.indexOf("(");
                        return resultSet.getString(columnLabel.substring(0, index)) ;
                    }
                    return resultSet.getString(columnLabel);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    throw exception;
                } catch (Exception e) {
                    String errorMsg = String.format(TYPE_CONVERT_ERROR_MESSAGE_TEMPLATE, columnLabel, "java.lang.String", resultSet.getString(columnLabel), e.getMessage());
                    throw new Exception(errorMsg);
                }
            }
        });
        return pair;
    }

    /**
     * 取浮点型字段值的方法
     *
     * @param columnLabel 字段名(或as后的别名)
     * @return
     */
    public Pair getPairForFloat(String columnLabel) {
        Pair<Class, GetColumnValueFunction<ResultSet, Float>> pair = Pair.of(java.lang.Float.class, new GetColumnValueFunction<ResultSet, Float>() {
            @Override
            public Float apply(ResultSet resultSet) throws Exception {
                try {
                    return resultSet.getFloat(columnLabel);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    throw exception;
                } catch (Exception e) {
                    String errorMsg = String.format(TYPE_CONVERT_ERROR_MESSAGE_TEMPLATE, columnLabel, "java.lang.Float", resultSet.getString(columnLabel), e.getMessage());
                    throw new Exception(errorMsg);
                }
            }
        });
        return pair;
    }

    /**
     * 取浮点型字段值的方法
     *
     * @param columnLabel 字段名(或as后的别名)
     * @return
     */
    public Pair getPairForDouble(String columnLabel) {
        Pair<Class, GetColumnValueFunction<ResultSet, Double>> pair = Pair.of(java.lang.Double.class, new GetColumnValueFunction<ResultSet, Double>() {
            @Override
            public Double apply(ResultSet resultSet) throws Exception {
                try {
                    return resultSet.getDouble(columnLabel);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    throw exception;
                } catch (Exception e) {
                    String errorMsg = String.format(TYPE_CONVERT_ERROR_MESSAGE_TEMPLATE, columnLabel, "java.lang.Double", resultSet.getString(columnLabel), e.getMessage());
                    throw new Exception(errorMsg);
                }
            }
        });
        return pair;
    }

    /**
     * 取整形字段值的方法
     *
     * @param columnLabel 字段名(或as后的别名)
     * @return
     */
    public Pair getPairForInteger(String columnLabel) {
        Pair<Class, GetColumnValueFunction<ResultSet, Integer>> pair = Pair.of(java.lang.Integer.class, new GetColumnValueFunction<ResultSet, Integer>() {
            @Override
            public Integer apply(ResultSet resultSet) throws Exception {
                try {
                    return resultSet.getInt(columnLabel);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    throw exception;
                } catch (Exception e) {
                    String errorMsg = String.format(TYPE_CONVERT_ERROR_MESSAGE_TEMPLATE, columnLabel, "java.lang.Integer", resultSet.getString(columnLabel), e.getMessage());
                    throw new Exception(errorMsg);
                }
            }
        });
        return pair;
    }

    /**
     * 取日期型字段值的方法
     *
     * @param columnLabel 字段名(或as后的别名)
     * @return
     */
    public Pair getPairForDate(String columnLabel) {
        Pair<Class, GetColumnValueFunction<ResultSet, Date>> pair = Pair.of(java.util.Date.class, new GetColumnValueFunction<ResultSet, Date>() {
            @Override
            public Date apply(ResultSet resultSet) throws Exception {
                try {
                    return resultSet.getDate(columnLabel);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    throw exception;
                } catch (Exception e) {
                    String errorMsg = String.format(TYPE_CONVERT_ERROR_MESSAGE_TEMPLATE, columnLabel, "java.lang.Date", resultSet.getString(columnLabel), e.getMessage());
                    throw new Exception(errorMsg);
                }
            }
        });
        return pair;
    }

    /**
     * 取长整型字段值的方法
     *
     * @param columnLabel 字段名(或as后的别名)
     * @return
     */
    public Pair getPairForLong(String columnLabel) {
        Pair<Class, GetColumnValueFunction<ResultSet, Long>> pair = Pair.of(java.lang.Long.class, new GetColumnValueFunction<ResultSet, Long>() {
            @Override
            public Long apply(ResultSet resultSet) throws Exception {
                try {
                    return resultSet.getLong(columnLabel);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    throw exception;
                } catch (Exception e) {
                    String errorMsg = String.format(TYPE_CONVERT_ERROR_MESSAGE_TEMPLATE, columnLabel, "java.lang.Long", resultSet.getString(columnLabel), e.getMessage());
                    throw new Exception(errorMsg);
                }
            }
        });
        return pair;
    }

    /**
     * 取布尔字段值的方法
     *
     * @param columnLabel 字段名(或as后的别名)
     * @return
     */
    public Pair getPairForBoolean(String columnLabel) {
        Pair<Class, GetColumnValueFunction<ResultSet, Boolean>> pair = Pair.of(java.lang.Boolean.class, new GetColumnValueFunction<ResultSet, Boolean>() {
            @Override
            public Boolean apply(ResultSet resultSet) throws Exception {
                try {
                    return resultSet.getBoolean(columnLabel);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    throw exception;
                } catch (Exception e) {
                    String errorMsg = String.format(TYPE_CONVERT_ERROR_MESSAGE_TEMPLATE, columnLabel, "java.lang.Boolean", resultSet.getString(columnLabel), e.getMessage());
                    throw new Exception(errorMsg);
                }
            }
        });
        return pair;
    }

    /**
     * 取未知类型字段值的方法
     *
     * @param columnLabel 字段名(或as后的别名)
     * @return
     */
    public Pair getPairForUnknownType(String columnLabel) {
        Pair<Class, GetColumnValueFunction<ResultSet, Object>> pair = Pair.of(java.lang.Object.class, new GetColumnValueFunction<ResultSet, Object>() {
            @Override
            public Object apply(ResultSet resultSet) throws Exception {
                try {
                    log.warn("抽取字段值,字段:[{}],由于未匹配到定义好的SQL类型,将原样返回", columnLabel);
                    return resultSet.getObject(columnLabel);
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    throw exception;
                }
            }
        });
        return pair;
    }
}

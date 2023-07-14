package com.kanyun.sql.core.column;

import com.kanyun.sql.core.JsonTable;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.type.SqlTypeName;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字段类型枚举
 * 需要保证枚举的值与{@link SqlTypeName }中定义的一致
 * 不一致的需要在使用的位置自定义区分,详见代码 {@link JsonTable} getRowType()
 * 创建 {@link RelDataType}
 */
public enum ColumnType {

    UNKNOWN("unknown"),
    BOOLEAN("boolean"),
    BIGINT("bigint"),
    INTEGER("integer"),
    FLOAT("float"),
    DOUBLE("double"),
    VARCHAR("varchar");
    String code;

    ColumnType(String code) {
        this.code = code;
    }

    public String toCode() {
        return code;
    }

    /**
     * 得到code的集合数组
     * @return
     */
    public static String[] codes() {
        List<String> codes = Arrays.stream(values()).map(x -> x.toCode()).collect(Collectors.toList());
        return codes.toArray(new String[codes.size()]);
    }

    /**
     * 根据Code获取枚举
     * @param code
     * @return
     */
    public static ColumnType getColumnTypeByCode(String code) {
        for (ColumnType type : ColumnType.values()) {
            if (type.toCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(codes()));
        System.out.println(ColumnType.valueOf("FLOAT"));
    }
}

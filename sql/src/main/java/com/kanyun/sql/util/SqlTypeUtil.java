package com.kanyun.sql.util;

import org.apache.calcite.avatica.util.ByteString;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactoryImpl;
import org.apache.calcite.runtime.FunctionContexts;
import org.apache.calcite.sql.type.BasicSqlType;
import org.apache.calcite.sql.type.IntervalSqlType;
import org.apache.calcite.sql.type.SqlTypeName;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/**
 * SQL类型转换,将SQL中使用的类型转换为Java类型(包装类型)
 */
public class SqlTypeUtil {


    /**
     * boolean            Boolean
     * char               Character
     * byte               Byte
     * short              Short
     * int                Integer
     * long               Long
     * float              Float
     * double             Double
     *
     * @param orgType
     * @return
     */
    public static Type convert(Type orgType) {
        if (orgType == boolean.class) {
            return Boolean.class;
        } else if (orgType == char.class) {
            return Character.class;
        } else if (orgType == Byte.class) {
            return Byte.class;
        } else if (orgType == int.class) {
            return Integer.class;
        } else if (orgType == long.class) {
            return Long.class;
        } else if (orgType == float.class) {
            return Float.class;
        } else if (orgType == double.class) {
            return Double.class;
        }
        return orgType;
    }

    public static Type getJavaClass(RelDataType type) {
        if (type instanceof RelDataTypeFactoryImpl.JavaType) {
            RelDataTypeFactoryImpl.JavaType javaType = (RelDataTypeFactoryImpl.JavaType) type;
            return javaType.getJavaClass();
        }
        if (type instanceof BasicSqlType || type instanceof IntervalSqlType) {
            switch (type.getSqlTypeName()) {
                case VARCHAR:
                case CHAR:
                    return String.class;
                case DATE:
                case TIME:
                case TIMESTAMP:
                    return Date.class;
                case TIME_WITH_LOCAL_TIME_ZONE:
                case INTEGER:
                case INTERVAL_YEAR:
                case INTERVAL_YEAR_MONTH:
                case INTERVAL_MONTH:
                    return type.isNullable() ? Integer.class : int.class;
                case TIMESTAMP_WITH_LOCAL_TIME_ZONE:
                case BIGINT:
                case INTERVAL_DAY:
                case INTERVAL_DAY_HOUR:
                case INTERVAL_DAY_MINUTE:
                case INTERVAL_DAY_SECOND:
                case INTERVAL_HOUR:
                case INTERVAL_HOUR_MINUTE:
                case INTERVAL_HOUR_SECOND:
                case INTERVAL_MINUTE:
                case INTERVAL_MINUTE_SECOND:
                case INTERVAL_SECOND:
                    return type.isNullable() ? Long.class : long.class;
                case SMALLINT:
                    return type.isNullable() ? Short.class : short.class;
                case TINYINT:
                    return type.isNullable() ? Byte.class : byte.class;
                case DECIMAL:
                    return BigDecimal.class;
                case BOOLEAN:
                    return type.isNullable() ? Boolean.class : boolean.class;
                case DOUBLE:
                case FLOAT: // sic
                    return type.isNullable() ? Double.class : double.class;
                case REAL:
                    return type.isNullable() ? Float.class : float.class;
                case BINARY:
                case VARBINARY:
                    return ByteString.class;
//                case GEOMETRY:
//                    return GeoFunctions.Geom.class;
                case SYMBOL:
                    return Enum.class;
                case ANY:
                    return Object.class;
            }
        }

        return null;
    }

    public static Class<? extends Object> sqlTypeToJavaType(SqlTypeName sqlTypeName) {
        switch (sqlTypeName) {
            case VARCHAR:
            case CHAR:
                return String.class;
            case DATE:
            case TIME:
                return Date.class;
            case TIMESTAMP:
                return Timestamp.class;
            case TIME_WITH_LOCAL_TIME_ZONE:
            case INTEGER:
            case INTERVAL_YEAR:
            case INTERVAL_YEAR_MONTH:
            case INTERVAL_MONTH:
                return Integer.class;
            case TIMESTAMP_WITH_LOCAL_TIME_ZONE:
            case BIGINT:
            case INTERVAL_DAY:
            case INTERVAL_DAY_HOUR:
            case INTERVAL_DAY_MINUTE:
            case INTERVAL_DAY_SECOND:
            case INTERVAL_HOUR:
            case INTERVAL_HOUR_MINUTE:
            case INTERVAL_HOUR_SECOND:
            case INTERVAL_MINUTE:
            case INTERVAL_MINUTE_SECOND:
            case INTERVAL_SECOND:
                return Long.class;
            case SMALLINT:
                return Short.class;
            case TINYINT:
                return Byte.class;
            case DECIMAL:
                return BigDecimal.class;
            case BOOLEAN:
                return Boolean.class;
            case DOUBLE:
            case FLOAT: // sic
                return Double.class;
            case REAL:
                return Float.class;
            case BINARY:
            case VARBINARY:
                return ByteString.class;
//            case GEOMETRY:
//                return GeoFunctions.Geom.class;
            case SYMBOL:
                return Enum.class;
            case ANY:
                return Object.class;
        }
        return null;
    }
}

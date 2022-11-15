package com.kanyun.sql.func;

/**
 * 函数来源选择接口
 */
public interface FuncSelection {

    AbstractFuncSource newInstance() throws InstantiationException, IllegalAccessException;
}

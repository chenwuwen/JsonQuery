package com.kanyun.sql.core;

/**
 * 获取列值的函数式接口,
 * 仿照java.util.function.Function 函数式接口,添加了抛出异常操作
 * @param <T>
 * @param <R>
 */
@FunctionalInterface
public interface GetColumnValueFunction<T, R> {

    R apply(T t) throws Exception;
}

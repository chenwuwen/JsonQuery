package com.kanyun.sql.hr;

import org.apache.calcite.schema.impl.AbstractSchema;

/**
 * 定义一个 Schema 结构用于表示存放数据的结构是什么样子的
 * 这里定义了一个叫 JavaHrSchema 的 schema 注意:此schema并未实现 {@link AbstractSchema} 可以参照 {@link com.kanyun.sql.simple.SimpleSchema} 的实现
 * 可以把它类比成数据库里面的一个 DB 实例。
 * 该 Schema 内有 Employee {@link Employee} 和 Department  {@link Department}  两张 table
 * 可以把它们理解成数据库里的表,
 * 最后在内存里给这两张表初始化了一些数据。
 */
public class JavaHrSchema  {
    public final Employee[] employee = {
            new Employee(100, "joe", 1),
            new Employee(200, "oliver", 2),
            new Employee(300, "twist", 1),
            new Employee(301, "king", 3),
            new Employee(305, "kelly", 1)
    };

    public final Department[] department = {
            new Department(1, "dev"),
            new Department(2, "market"),
            new Department(3, "test")
    };

}

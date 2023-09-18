package com.kanyun.sql.func;

import com.kanyun.sql.util.ClassUtil;
import com.kanyun.sql.util.SqlTypeUtil;
import org.apache.calcite.adapter.enumerable.*;
import org.apache.calcite.linq4j.tree.*;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of{@link StrictAggImplementor}
 * that calls a given {@link Method} [init, add, result] method of DynamicUDAFProxy.
 *
 * <p>When udaf init,add,result method is not static, a new instance of the required class is created.
 */
public class DynamicUDAFReflectiveImplementor extends StrictAggImplementor {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DynamicUDAFReflectiveImplementor.class);
    private final DynamicAggFunctionImpl afi;

    public DynamicUDAFReflectiveImplementor(DynamicAggFunctionImpl afi) {
        this.afi = afi;
    }

    @Override
    public List<Type> getNotNullState(AggContext info) {
        if (afi.isStatic) {
            return Collections.singletonList(afi.accumulatorType);
        }
        return Arrays.asList(afi.accumulatorType, afi.declaringClass);
    }

    @Override
    protected void implementNotNullReset(AggContext info,
                                         AggResetContext reset) {
        logger.info("init method implement..........");
        List<Expression> acc = reset.accumulator();
        Expression udaf = getDynamicUDAFExpression(afi.declaringClass);
        if (!afi.isStatic) {
            reset.currentBlock().add(
                    Expressions.statement(
                            Expressions.assign(acc.get(1),
                                    Expressions.new_(afi.declaringClass))));
        }

        Expression expression = Expressions.assign(acc.get(0),
                Expressions.convert_(Expressions.call(afi.isStatic
                        ? null
                        : acc.get(1), afi.initMethod, udaf), SqlTypeUtil.convert((afi.getMethodReturnTypeByName("init")))));
        Statement statement = Expressions.statement(expression);
        //afi.dynamicFunction
        reset.currentBlock().add(statement);
    }

    @Override
    protected void implementNotNullAdd(AggContext info,
                                       AggAddContext add) {
        logger.info("add method implement..........");
//        返回应重置的累加器变量
        List<Expression> acc = add.accumulator();
//        返回add函数内的参数(即字段,或者自定义的常量)
        List<Expression> aggArgs = add.arguments();
        List<Expression> args = new ArrayList<>(aggArgs.size() + 2);
        Expression udaf = getDynamicUDAFExpression(afi.declaringClass);

        args.add(udaf);
        args.add(acc.get(0));
        args.addAll(aggArgs);
//        创建赋值的BinaryExpression
        Expression expression = Expressions.assign(acc.get(0),
                Expressions.convert_(Expressions.call(afi.isStatic ? null : acc.get(1),
                        afi.addMethod, args),
                        SqlTypeUtil.convert(afi.getMethodReturnTypeByName("add"))));
//        创建一个可以执行表达式的代码声明
        Statement statement = Expressions.statement(expression);
        String code = statement.toString();
//        将声明的代码添加到当前的代码块中
        add.currentBlock().add(statement);
    }

    @Override
    protected Expression implementNotNullResult(AggContext info,
                                                AggResultContext result) {
        logger.info("result method implement..........");
        List<Expression> args = new ArrayList<>(2);
        List<Expression> acc = result.accumulator();
        Expression udaf = getDynamicUDAFExpression(afi.declaringClass);
        args.add(udaf);
        args.add(acc.get(0));
        Expression expression = Expressions.call(
                afi.isStatic ? null : acc.get(1), afi.resultMethod, args);
        return Expressions.convert_(expression, SqlTypeUtil.convert(info.returnType()));
    }

    /**
     * 通过构造函数,生成 declaringClass 实例的表达式
     *
     * @param declaringClass
     * @return
     */
    private Expression getDynamicUDAFExpression(Class<?> declaringClass) {
        Method resultMethod = ClassUtil.findMethod(declaringClass, "result");
        Method initMethod = ClassUtil.findMethod(declaringClass, "init");
        Method addMethod = ClassUtil.findMethod(declaringClass, "add");
        Class<?> returnParamType = afi.resultMethod.getParameterTypes()[0];
        assert addMethod != null;
        assert initMethod != null;
//        Linq4j创建新对象的表达式,类似于Java中的new操作符,但可以在查询中使用
        return Expressions.new_(declaringClass,
//                Expressions.constant(declaringClass.getName(), String.class),
//                定义聚合函数的名称
//                Expressions.constant(afi.methodName, String.class),
                Expressions.constant(afi.initMethod.getReturnType()),
                Expressions.constant(afi.resultMethod.getReturnType()),
                Expressions.constant(afi.addMethod.getReturnType()),
                Expressions.constant(afi.addMethod.getParameterTypes(), Object.class),
                Expressions.constant(returnParamType, Class.class));
    }

    private Expression getDynamicUDAFExpression(Class<?> declaringClass,String methodName) {
        Method method = ClassUtil.findMethod(declaringClass, methodName);
        Class<?>[] parameterTypes = method.getParameterTypes();
        logger.info("method:{},returnType:{},paramType:{}",methodName,method.getReturnType(),parameterTypes);
        if (parameterTypes.length != 0) {
            //        Linq4j创建新对象的表达式,类似于Java中的new操作符,但可以在查询中使用
            return Expressions.new_(method.getReturnType(),
                    Expressions.constant(parameterTypes, Object[].class));
        }
        return Expressions.new_(method.getReturnType(),Expressions.constant(Void.class));
    }
}

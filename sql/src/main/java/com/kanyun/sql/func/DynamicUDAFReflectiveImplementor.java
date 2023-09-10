package com.kanyun.sql.func;

import com.kanyun.sql.util.SqlTypeUtil;
import org.apache.calcite.adapter.enumerable.*;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.linq4j.tree.Types;

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
 *
 */
public class DynamicUDAFReflectiveImplementor extends StrictAggImplementor {
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
        List<Expression> acc = reset.accumulator();
        Expression udaf = getDynamicUDAFExpression(afi.declaringClass);
        if (!afi.isStatic) {
            reset.currentBlock().add(
                    Expressions.statement(
                            Expressions.assign(acc.get(1),
                                    Expressions.new_(afi.declaringClass))));
        }

        //afi.dynamicFunction
        reset.currentBlock().add(
                Expressions.statement(
                        Expressions.assign(acc.get(0),
                                Expressions.convert_(Expressions.call(afi.isStatic
                                        ? null
                                        : acc.get(1), afi.initMethod, udaf), SqlTypeUtil.convert((afi.getMethodReturnTypeByName("init")))))));
    }

    @Override
    protected void implementNotNullAdd(AggContext info,
                                       AggAddContext add) {
        List<Expression> acc = add.accumulator();
        List<Expression> aggArgs = add.arguments();
        List<Expression> args = new ArrayList<>(aggArgs.size() + 2);
        Expression udaf = getDynamicUDAFExpression(afi.declaringClass);
        //Class<?> resultType = afi.dynamicFunction.getReturnType(DynamicFunctionUtils.ADD);

        args.add(udaf);
        args.add(acc.get(0));
        args.addAll(aggArgs);
        add.currentBlock().add(
                Expressions.statement(
                        Expressions.assign(acc.get(0),
                                Expressions.convert_(Expressions.call(afi.isStatic ? null : acc.get(1), afi.addMethod, args), SqlTypeUtil.convert(afi.getMethodReturnTypeByName("add"))))));
    }

    @Override
    protected Expression implementNotNullResult(AggContext info,
                                                AggResultContext result) {
        List<Expression> args = new ArrayList<>(2);
        List<Expression> acc = result.accumulator();
        Expression udaf = getDynamicUDAFExpression(afi.declaringClass);
        args.add(udaf);
        args.add(acc.get(0));
        return Expressions.convert_(Expressions.call(
                afi.isStatic ? null : acc.get(1), afi.resultMethod, args), SqlTypeUtil.convert(info.returnType()));
    }

    /**
     * 通过构造函数,生成 declaringClass 实例的表达式
     * @param declaringClass
     * @return
     */
    private Expression getDynamicUDAFExpression(Class<?> declaringClass ) {
        Method resultMethod = Types.lookupMethod(declaringClass, "result");
        Method initMethod = Types.lookupMethod(declaringClass, "init");
        Method addMethod = Types.lookupMethod(declaringClass, "add");
        Class<?> returnParamType = resultMethod.getParameterTypes()[0];

        return Expressions.new_(declaringClass,
                Expressions.constant(declaringClass.getName(), String.class),
//                定义聚合函数的名称
                Expressions.constant(declaringClass.getName(), String.class),
                Expressions.constant(initMethod.getReturnType()),
                Expressions.constant(resultMethod.getReturnType()),
                Expressions.constant(addMethod.getReturnType()),
                Expressions.constant(addMethod.getParameterTypes(), List.class),
                Expressions.constant(returnParamType, Class.class));
    }
}

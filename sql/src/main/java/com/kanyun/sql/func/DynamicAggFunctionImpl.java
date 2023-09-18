package com.kanyun.sql.func;

import com.google.common.collect.ImmutableList;
import com.kanyun.sql.util.ClassUtil;
import org.apache.calcite.adapter.enumerable.AggImplementor;
import org.apache.calcite.adapter.enumerable.RexImpTable;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.AggregateFunction;
import org.apache.calcite.schema.FunctionParameter;
import org.apache.calcite.schema.ImplementableAggFunction;
import org.apache.calcite.schema.impl.AggregateFunctionImpl;
import org.apache.calcite.schema.impl.ReflectiveFunctionBase;
import org.apache.calcite.util.ReflectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 自定义动态聚合函数类,动态注册聚合函数需要使用 {@link AggregateFunction} 接口的实例
 * 参照 {@link AggregateFunctionImpl}
 */
public class DynamicAggFunctionImpl implements AggregateFunction,
        ImplementableAggFunction {

    private static final Logger logger = LoggerFactory.getLogger(DynamicAggFunctionImpl.class);

    public final boolean isStatic;
    public final Method initMethod;
    public final Method addMethod;
    public final Method mergeMethod;
    /**
     * 返回结果的方法,可能为空
     */
    public final Method resultMethod;
    public final ImmutableList<Class<?>> valueTypes;
    /**
     * 函数使用的参数
     */
    private final List<FunctionParameter> parameters;
    /**
     * 累加器类型(initMethod()定义的返回值类型)
     */
    public final Class<?> accumulatorType;
    /**
     * 结果类型(resultMethod()定义的返回值类型)
     */
    public final Class<?> resultType;
    /**
     * 自定义的聚合函数类
     */
    public final Class<?> declaringClass;

    /**
     * 动态聚合函数指定的函数名
     */
    public final String methodName;


    private DynamicAggFunctionImpl(Class<?> declaringClass,
                                   String methodName,
                                   List<FunctionParameter> params,
                                   List<Class<?>> valueTypes,
                                   Class<?> accumulatorType,
                                   Class<?> resultType,
                                   Method initMethod,
                                   Method addMethod,
                                   Method mergeMethod,
                                   Method resultMethod) {
        this.declaringClass = declaringClass;
        this.methodName = methodName;
        this.valueTypes = ImmutableList.copyOf(valueTypes);
        this.parameters = params;
        this.accumulatorType = accumulatorType;
        this.resultType = resultType;
        this.initMethod = Objects.requireNonNull(initMethod);
        this.addMethod = Objects.requireNonNull(addMethod);
        this.mergeMethod = mergeMethod;
        this.resultMethod = resultMethod;
        this.isStatic = Modifier.isStatic(initMethod.getModifiers());

        assert resultMethod != null || accumulatorType == resultType;
    }

    public static DynamicAggFunctionImpl create(Class clazz, String methodName) {
        final Method initMethod = ClassUtil.findMethod(clazz, "init");
        final Method addMethod = ClassUtil.findMethod(clazz, "add");
        final Method mergeMethod = ClassUtil.findMethod(clazz, "merge");
        final Method resultMethod = ClassUtil.findMethod(clazz, "result");
        // A is return type of init by definition
        final Class<?> accumulatorType = initMethod.getReturnType();

        // R is return type of result by definition
        final Class<?> resultType = resultMethod.getReturnType();

        // V is remaining args of add by definition
        final List<Class<?>> addParamTypes = Arrays.asList(addMethod.getParameterTypes());

//        构建函数使用的参数集合
        final ReflectiveFunctionBase.ParameterListBuilder params =
                ReflectiveFunctionBase.builder();

        final ImmutableList.Builder<Class<?>> valueTypes =
                ImmutableList.builder();
        for (int i = 1; i < addParamTypes.size(); i++) {
//            获取add方法参数类型
            final Class<?> paramType = addParamTypes.get(i);
//            获取add方法参数名
            final String paramName = ReflectUtil.getParameterName(addMethod, i);
//            获取add方法参数是否是可选的(即是否必传)
            final boolean paramOptional = ReflectUtil.isParameterOptional(addMethod, i);
            params.add(paramType, paramName, paramOptional);
            valueTypes.add(paramType);
        }
        logger.info("准备创建聚合函数");
        return new DynamicAggFunctionImpl(clazz, methodName, params.build(),
                valueTypes.build(), accumulatorType, resultType, initMethod,
                addMethod, mergeMethod, resultMethod);
    }

    /**
     * 返回将函数转换为linq4j表达式的实现器
     *
     * @param windowContext 在窗口上下文中使用聚合时为true
     * @return
     */
    @Override
    public AggImplementor getImplementor(boolean windowContext) {
        return new DynamicUDAFReflectiveImplementor(this);
    }

    @Override
    public RelDataType getReturnType(RelDataTypeFactory typeFactory) {
        return typeFactory.createJavaType(resultType);
    }

    @Override
    public List<FunctionParameter> getParameters() {
        return parameters;
    }

    public Type getMethodReturnTypeByName(String methodName) {
        return ClassUtil.findMethod(declaringClass, methodName).getReturnType();
    }

}

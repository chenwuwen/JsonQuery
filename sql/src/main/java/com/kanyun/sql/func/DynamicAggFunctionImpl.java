package com.kanyun.sql.func;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.adapter.enumerable.AggImplementor;
import org.apache.calcite.linq4j.tree.Types;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.AggregateFunction;
import org.apache.calcite.schema.FunctionParameter;
import org.apache.calcite.schema.ImplementableAggFunction;
import org.apache.calcite.schema.impl.ReflectiveFunctionBase;
import org.apache.calcite.util.ReflectUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 自定义动态聚合函数类,动态注册聚合函数需要使用 {@link AggregateFunction} 接口的实例
 */
public class DynamicAggFunctionImpl implements AggregateFunction,
        ImplementableAggFunction {

    public final boolean isStatic;
    public final Method initMethod;
    public final Method addMethod;
    public final Method mergeMethod;
    public final Method resultMethod; // may be null
    public final ImmutableList<Class<?>> valueTypes;
    private final List<FunctionParameter> parameters;
    public final Class<?> accumulatorType;
    public final Class<?> resultType;
    public final Class<?> declaringClass;


    private DynamicAggFunctionImpl(Class<?> declaringClass,
                                   List<FunctionParameter> params,
                                   List<Class<?>> valueTypes,
                                   Class<?> accumulatorType,
                                   Class<?> resultType,
                                   Method initMethod,
                                   Method addMethod,
                                   Method mergeMethod,
                                   Method resultMethod) {
        this.declaringClass = declaringClass;
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

    public static DynamicAggFunctionImpl create(Class clazz) {
        final Method initMethod = Types.lookupMethod(clazz, "init");
        final Method addMethod = Types.lookupMethod(clazz, "add");
        final Method mergeMethod = null; // TODO:
        final Method resultMethod = Types.lookupMethod(clazz, "result");
        // A is return type of init by definition
        final Class<?> accumulatorType = initMethod.getReturnType();

        // R is return type of result by definition
        final Class<?> resultType = resultMethod.getReturnType();

        // V is remaining args of add by definition
        final List<Class<?>> addParamTypes = Arrays.asList(addMethod.getParameterTypes());

        final ReflectiveFunctionBase.ParameterListBuilder params =
                ReflectiveFunctionBase.builder();
        final ImmutableList.Builder<Class<?>> valueTypes =
                ImmutableList.builder();
        for (int i = 1; i < addParamTypes.size(); i++) {
            final Class type = addParamTypes.get(i);
            final String name = ReflectUtil.getParameterName(addMethod, i);
            final boolean optional = ReflectUtil.isParameterOptional(addMethod, i);
            params.add(type, name, optional);
            valueTypes.add(type);
        }

        return new DynamicAggFunctionImpl(clazz, params.build(),
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
        return Types.lookupMethod(declaringClass, methodName).getReturnType();
    }
}

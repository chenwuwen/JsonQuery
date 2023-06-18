package com.kanyun.sql.simple;


import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelDistribution;
import org.apache.calcite.rel.RelDistributionTraitDef;
import org.apache.calcite.rel.RelReferentialConstraint;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.util.ImmutableBitSet;

import java.util.Collections;
import java.util.List;

/**
 * 在calcite中，Statistic类是用于表示表或列的统计信息的类。它包含有关表或列的各种元数据，
 * 如行数、列的唯一值个数、平均值、标准差、最小值、最大值等。
 * 这些统计信息可以帮助calcite的查询优化器更好地评估查询计划，并选择更优的执行路径。
 * 例如，优化器可以利用列的唯一值个数统计信息来选择使用哈希连接还是嵌套循环连接，
 * 或者根据列的平均值和标准差来选择使用哪种聚合函数（如SUM或AVG）
 */
public class SimpleTableStatistic implements Statistic {

    private final long rowCount;

    public SimpleTableStatistic(long rowCount) {
        this.rowCount = rowCount;
    }

    @Override
    public Double getRowCount() {
        return (double) rowCount;
    }

    @Override
    public boolean isKey(ImmutableBitSet columns) {
        return false;
    }

    @Override
    public List<ImmutableBitSet> getKeys() {
        return Collections.emptyList();
    }

    @Override
    public List<RelReferentialConstraint> getReferentialConstraints() {
        return Collections.emptyList();
    }

    @Override
    public List<RelCollation> getCollations() {
        return Collections.emptyList();
    }

    @Override
    public RelDistribution getDistribution() {
        return RelDistributionTraitDef.INSTANCE.getDefault();
    }
}
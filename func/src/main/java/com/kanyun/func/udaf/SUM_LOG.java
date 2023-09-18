package com.kanyun.func.udaf;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚合函数,对对数的结果累加 这里的对数指的是自然对数(自然对数是以常数e为底数的对数,即ln)
 * 对每行(指定字段的数值求取对数,并计算SUM值)
 */
public class SUM_LOG {

    public static List<Double> init() {
        return new ArrayList<>();
    }

    public static List<Double> add(List<Double> list, Double v) {
        list.add(v);
        return list;
    }

    public static List<Double> merge(List<Double> list1, List<Double> list2) {
        list1.addAll(list2);
        return list1;
    }

    public static double result(List<Double> list) {
        double initValue = 0.0;

        for (Double value : list) {
//            即 ln(x) ,底数是常量e,e是一个无限不循环小数，其值约等于2.718281828
            initValue += Math.log(value);
        }

        return initValue;
    }

    public static void main(String[] args) {
            List<Double> testList = new ArrayList<>();
            testList.add(1.0); // log(1.0) = 0
//          Math.exp(a)这里的exp是一个方法，它的功能是返回e的a次方
            testList.add(Math.exp(2)); // log(Math.exp(2)) = 2
            testList.add(2.7182818284590452354); // log(2.7182818284590452354) = 1
            System.out.println(result(testList));
    }
}

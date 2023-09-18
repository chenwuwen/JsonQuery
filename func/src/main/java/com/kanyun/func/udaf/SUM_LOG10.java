package com.kanyun.func.udaf;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚合函数,对对数的结果累加 这里的对数指的是以10为底数的对数,即log)
 * 对每行(指定字段的数值求取对数,并计算SUM值)
 */
public class SUM_LOG10 {

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
//            即 log(x) log的缩写是logarithms，一般默认以10为底数
            initValue += Math.log10(value);
        }

        return initValue;
    }

    public static void main(String[] args) {
            List<Double> testList = new ArrayList<>();
            testList.add(10.0); // log(10.0) = 1
            testList.add(100.0); // log(100.0) = 2
            testList.add(1000.0); // log(1000.0) = 3
            System.out.println(result(testList));
    }
}

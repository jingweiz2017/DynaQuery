package com.jingweizhang.dynaquery.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Description
 * Entity that maps to Group_Object table
 *
 * @Author jingwei.zhang on 2023/4/3
 */
@Data
@NoArgsConstructor
public class GroupBy implements Serializable {
    private List<String> fields;

    private Aggregator aggregator;

    private FilterCondition having;

    private String alias;

    public GroupBy(List<String> fields, Aggregator aggregator, FilterCondition having, String alias) {
        this.fields = fields;
        this.aggregator = aggregator;
        this.having = having;
        this.alias = alias;
    }

    public static GroupBy of(List<String> fields, Aggregator aggregator, FilterCondition having, String alias) {
        return new GroupBy(fields, aggregator, having, alias);
    }

    public static GroupBy of(List<String> fields, Aggregator aggregator, FilterCondition having) {
        return new GroupBy(fields, aggregator, having, null);
    }

    public static GroupBy of(List<String> fields, Aggregator aggregator, String alias) {
        return new GroupBy(fields, aggregator, null, alias);
    }

    public static GroupBy of(List<String> fields, Aggregator aggregator) {
        return new GroupBy(fields, aggregator, null, null);
    }
}

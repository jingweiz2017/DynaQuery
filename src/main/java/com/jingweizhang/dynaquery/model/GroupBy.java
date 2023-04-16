package com.jingweizhang.dynaquery.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Description
 * Entity that maps to Group_Object table
 *
 * @Author rocky.zhang on 2023/4/3
 */
@Data
@NoArgsConstructor
public class GroupBy implements Serializable {
    private List<String> fields;

    private Aggregator aggregator;

    private Filter having;

    public GroupBy(List<String> fields, Aggregator aggregator, Filter having) {
        this.fields = fields;
        this.aggregator = aggregator;
        this.having = having;
    }

    public static GroupBy of(List<String> fields, Aggregator aggregator, Filter having) {
        return new GroupBy(fields, aggregator, having);
    }

    public static GroupBy of(List<String> fields, Aggregator aggregator) {
        return new GroupBy(fields, aggregator, null);
    }
}

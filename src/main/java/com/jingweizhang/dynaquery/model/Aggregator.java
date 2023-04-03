package com.jingweizhang.dynaquery.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * For aggregation, group by operation in sql
 *
 * @Author rocky.zhang on 2023/3/30
 */
@Data
@NoArgsConstructor
public class Aggregator {
    private String field;
    private AggregateOperator operator;

    public Aggregator(String field, AggregateOperator operator) {
        this.field = field;
        this.operator = operator;
    }

    public static Aggregator of(String field, AggregateOperator operator) {
        return new Aggregator(field, operator);
    }
}

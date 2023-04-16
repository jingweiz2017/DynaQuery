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
    private String alias;

    public Aggregator(String field, AggregateOperator operator, String alias) {
        this.field = field;
        this.operator = operator;
        this.alias = alias;
    }

    public static Aggregator of(String field, AggregateOperator operator, String alias) {
        return new Aggregator(field, operator, alias);
    }

    public String getAlias() {
        return this.alias != null ?
                this.alias : this.getOperator().name().toLowerCase() + "_" + this.getField();
    }
}

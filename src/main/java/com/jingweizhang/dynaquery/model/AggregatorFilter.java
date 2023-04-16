package com.jingweizhang.dynaquery.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * @Author rocky.zhang on 2023/4/15
 */
@Data
@NoArgsConstructor
public class AggregatorFilter implements Filter {
    private Aggregator aggregator;

    private FilterOperator filterOperator;
    private List<Object> values;

    public AggregatorFilter(Aggregator aggregator, FilterOperator filterOperator, List<Object> values) {
        this.aggregator = aggregator;
        this.filterOperator = filterOperator;
        this.values = values;
    }

    public static AggregatorFilter of(Aggregator aggregator, FilterOperator filterOperator, List<Object> values) {
        return new AggregatorFilter(aggregator, filterOperator, values);
    }
}

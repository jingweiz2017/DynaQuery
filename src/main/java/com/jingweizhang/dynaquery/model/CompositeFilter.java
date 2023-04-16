package com.jingweizhang.dynaquery.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * BinaryFilterCondition is used to connect two filter conditions with AND/OR operator.
 *
 * @Author rocky.zhang on 2023/3/15
 */
@Data
@NoArgsConstructor
public class CompositeFilter implements Filter {
    private List<Filter> filters;
    private FilterConnector connector;

    public CompositeFilter(List<Filter> filters, FilterConnector connector) {
        this.filters = filters;
        this.connector = connector;
    }


    public List<Filter> getFilters() {
        return filters;
    }

    public FilterConnector getConnector() {
        return connector;
    }

    public static CompositeFilter of(List<Filter> filters, FilterConnector connector) {
        return new CompositeFilter(filters, connector);
    }
}

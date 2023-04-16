package com.jingweizhang.dynaquery.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Description
 * Entity that maps to Filter_Object table
 *
 * @Author rocky.zhang on 2023/4/3
 */
@Data
@NoArgsConstructor
public class SimpleFilter implements Filter, Serializable {
    private String field;
    private FilterOperator filterOperator;
    private List<Object> values;

    public SimpleFilter(String field, FilterOperator filterOperator, List<Object> values) {
        this.field = field;
        this.filterOperator = filterOperator;
        this.values = values;
    }

    public static SimpleFilter of(String column, FilterOperator filterOperator, List<Object> values) {
        return new SimpleFilter(column, filterOperator, values);
    }
}

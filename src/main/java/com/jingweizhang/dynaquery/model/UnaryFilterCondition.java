package com.jingweizhang.dynaquery.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * UnaryFilterCondition is used to express one set of filters.
 * The relationship among those filters is logic "AND".
 *
 * @Author rocky.zhang on 2023/3/15
 */
@Data
@NoArgsConstructor
public class UnaryFilterCondition implements FilterCondition {
    private List<FilterBy> filters;

    public UnaryFilterCondition(List<FilterBy> filters) {
        this.filters = filters;
    }

    public List<FilterBy> getFilters() {
        return filters;
    }
}

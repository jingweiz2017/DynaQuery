package com.jingweizhang.dynaquery.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description
 * Entity that maps to Sort_Object table
 *
 * @Author jingwei.zhang on 2023/4/3
 */
@Data
@NoArgsConstructor
public class OrderBy implements Serializable {
    private String field;

    private SortingOperator operator;

    private int sequence;

    public OrderBy(String field, SortingOperator operator, int sequence) {
        this.field = field;
        this.operator = operator;
        this.sequence = sequence;
    }

    public static OrderBy of(String field, SortingOperator sorting, int sequence) {
        return new OrderBy(field, sorting, sequence);
    }
}

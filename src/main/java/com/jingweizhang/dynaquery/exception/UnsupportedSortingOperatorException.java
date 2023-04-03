package com.jingweizhang.dynaquery.exception;

/**
 * @Description
 * @Author rocky.zhang on 2023/3/15
 */
public class UnsupportedSortingOperatorException extends AbstractDynaQueryException {
    public UnsupportedSortingOperatorException(String field, String operator) {
        super(String.format("Unsupported Sorting Operator: %s on field %s", operator, field));
    }
}

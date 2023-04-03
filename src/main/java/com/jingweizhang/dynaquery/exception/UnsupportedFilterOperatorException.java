package com.jingweizhang.dynaquery.exception;

/**
 * @Description
 * @Author rocky.zhang on 2023/3/15
 */
public class UnsupportedFilterOperatorException extends AbstractDynaQueryException {
    public UnsupportedFilterOperatorException(String field, String filterOperator) {
        super(String.format("Unsupported Filter Operator: %s on Field %s", filterOperator, field));
    }
}

package com.jingweizhang.dynaquery.exception;

/**
 * @Description
 * @Author rocky.zhang on 2023/4/16
 */
public class InvalidFilterException extends AbstractDynaQueryException {
    private static final long serialVersionUID = 1L;
    public InvalidFilterException(String invalidFilterType) {
        super(String.format("Invalid filter type %s", invalidFilterType));
    }
}

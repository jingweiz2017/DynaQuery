package com.jingweizhang.dynaquery.exception;

/**
 * @Description
 * @Author rocky.zhang on 2023/4/7
 */
public class InvalidFieldAliasException extends AbstractDynaQueryException {
    public InvalidFieldAliasException(String field) {
        super(String.format("Field alias: %s can't be null or empty", field));
    }
}


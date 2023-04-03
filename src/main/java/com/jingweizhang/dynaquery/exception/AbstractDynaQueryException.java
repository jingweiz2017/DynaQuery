package com.jingweizhang.dynaquery.exception;

/**
 * @Description
 * @Author rocky.zhang on 2023/3/24
 */
public abstract class AbstractDynaQueryException extends RuntimeException {
    public AbstractDynaQueryException(String message) {
        super(message);
    }
}

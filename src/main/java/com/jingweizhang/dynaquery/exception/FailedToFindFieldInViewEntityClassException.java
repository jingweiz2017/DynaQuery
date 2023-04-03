package com.jingweizhang.dynaquery.exception;

/**
 * @Description
 * @Author rocky.zhang on 2023/3/15
 */
public class FailedToFindFieldInViewEntityClassException extends AbstractDynaQueryException {
    public FailedToFindFieldInViewEntityClassException(String clazzName, String fieldName) {
        super(String.format("Failed to find Field %s in Entity %s", fieldName, clazzName));
    }
}

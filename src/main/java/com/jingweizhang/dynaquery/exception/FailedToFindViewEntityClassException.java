package com.jingweizhang.dynaquery.exception;

/**
 * @Description
 * @Author rocky.zhang on 2023/3/15
 */
public class FailedToFindViewEntityClassException extends RuntimeException {
    private final static String message = "Failed to find ViewEntity %s";

    public FailedToFindViewEntityClassException(Exception ex) {
        super(ex);
    }

    public FailedToFindViewEntityClassException(String viewEntityClazz) {
        super(String.format(message, viewEntityClazz));
    }
}

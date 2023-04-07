package com.jingweizhang.dynaquery.exception;

/**
 * @Description
 * @Author rocky.zhang on 2023/3/15
 */
public class InvalidViewEntityException extends RuntimeException {
    private final static String message = "Invalid ViewEntity. %s";

    public InvalidViewEntityException(Exception ex) {
        super(ex);
    }

    public InvalidViewEntityException(String viewEntityClazz) {
        super(String.format(message, viewEntityClazz));
    }
}

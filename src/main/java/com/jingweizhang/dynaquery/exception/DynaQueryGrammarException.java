package com.jingweizhang.dynaquery.exception;

/**
 * @Description
 * @Author rocky.zhang on 2023/3/30
 */
public class DynaQueryGrammarException extends AbstractDynaQueryException {
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE = "Variant Query Grammar Exception. SQL State Code: %s";
    public DynaQueryGrammarException(String sqlStateCode) {
        super(String.format(MESSAGE, sqlStateCode));
    }
}

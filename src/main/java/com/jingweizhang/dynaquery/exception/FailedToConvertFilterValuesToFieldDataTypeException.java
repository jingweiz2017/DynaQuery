package com.jingweizhang.dynaquery.exception;

/**
 * @Description
 * @Author rocky.zhang on 2023/3/17
 */
public class FailedToConvertFilterValuesToFieldDataTypeException extends AbstractDynaQueryException {
    public FailedToConvertFilterValuesToFieldDataTypeException(String fieldName, String dataType) {
        super(String.format("Failed To Convert Filter Values for Field %s with Data Type %s", fieldName, dataType));
    }
}

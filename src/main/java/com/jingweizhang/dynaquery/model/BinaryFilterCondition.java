package com.jingweizhang.dynaquery.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * BinaryFilterCondition is used to connect two filter conditions with AND/OR operator.
 *
 * @Author rocky.zhang on 2023/3/15
 */
@Data
@NoArgsConstructor
public class BinaryFilterCondition implements FilterCondition{
    private FilterCondition left;
    private FilterConditionConnector connector;
    private FilterCondition right;

    public BinaryFilterCondition(FilterCondition left, FilterConditionConnector connector, FilterCondition right) {
        this.left = left;
        this.connector = connector;
        this.right = right;
    }


    public FilterCondition getLeft() {
        return left;
    }

    public FilterConditionConnector getConnector() {
        return connector;
    }

    public FilterCondition getRight() {
        return right;
    }

    public static BinaryFilterCondition of(FilterCondition left, FilterConditionConnector connector, FilterCondition right) {
        return new BinaryFilterCondition(left, connector, right);
    }
}

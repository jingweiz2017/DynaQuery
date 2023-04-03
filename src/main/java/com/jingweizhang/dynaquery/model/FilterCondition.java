package com.jingweizhang.dynaquery.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @Description
 * @Author rocky.zhang on 2023/3/15
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonSubTypes({ @JsonSubTypes.Type(UnaryFilterCondition.class), @JsonSubTypes.Type(BinaryFilterCondition.class) })
public interface FilterCondition {
}

package com.jingweizhang.dynaquery.model;

/**
 * @Description
 * Operator that connect a column with its value.
 * Used in Filter.
 *
 * @Author rocky.zhang on 2023/4/3
 */
public enum FilterOperator {
    EQ("="),
    LT("<"),
    LE("<="),
    GT(">"),
    GE(">="),
    NE("<>"),
    LIKE("LIKE"),
    NOTLIKE("NOT LIKE"),
    IN("IN"),
    NOTIN("NOT IN"),
    BETWEEN("BETWEEN"),
    ISNULL("IS NULL"),
    NOTNULL("IS NOT NULL"),
    ELEMATCH("element match");

    private String desc;

    FilterOperator(String desc) {
        this.desc = desc;
    }
}

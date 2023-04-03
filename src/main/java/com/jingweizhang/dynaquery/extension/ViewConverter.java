package com.jingweizhang.dynaquery.extension;

/**
 * @Description
 * ViewConverter defines the interface used by VariantQueryService for converting a query result to view object.
 *
 * @Author rocky.zhang on 2023/3/25
 */
public interface ViewConverter<E, V> {
    V convert(E viewEntity);
}

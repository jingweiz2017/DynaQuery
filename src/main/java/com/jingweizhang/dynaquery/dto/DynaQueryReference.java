package com.jingweizhang.dynaquery.dto;

import com.jingweizhang.dynaquery.model.DynaQuery;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Description
 * @Author jingwei.zhang on 2023/4/3
 */
@Data
@AllArgsConstructor
public class DynaQueryReference {
    private String id;
    private String name;
    private boolean isDefault;

    public static DynaQueryReference of(DynaQuery dynaQuery) {
        return new DynaQueryReference(dynaQuery.getId().toString(),
                dynaQuery.getName(),
                dynaQuery.getIsDefault()
        );
    }
}

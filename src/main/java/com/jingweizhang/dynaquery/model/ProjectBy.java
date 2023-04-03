package com.jingweizhang.dynaquery.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description
 * Entity that maps to Column_Object table
 *
 * @Author jingwei.zhang on 2023/4/3
 */
@Data
@NoArgsConstructor
public class ProjectBy implements Serializable {
    private String field;

    private boolean isVisible;

    public ProjectBy(String field, boolean isVisible) {
        this.field = field;
        this.isVisible = isVisible;
    }

    public static ProjectBy of(String field, boolean isVisible) {
        return new ProjectBy(field, isVisible);
    }
}

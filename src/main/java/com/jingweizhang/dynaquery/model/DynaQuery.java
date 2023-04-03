package com.jingweizhang.dynaquery.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @Description
 * Entity that maps to Variant_Object table
 * And it is THE query itself for an on the fly request if no persistent needed.
 *
 * @Author jingwei.zhang on 2023/4/3
 */
@Data
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@Entity
@Table(name = "t_dyna_query")
public class DynaQuery implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", columnDefinition = "varchar(20)", nullable = false)
    private String name;

    @Column(name = "is_default", columnDefinition = "bool default false", nullable = false)
    private Boolean isDefault;

    @Column(name = "project_by", columnDefinition = "json")
    @Type(type = "json")
    private List<ProjectBy> projectBys;

    @Column(name = "filter_condition", columnDefinition = "json")
    @Type(type = "json")
    private FilterCondition filterCondition;

    @Column(name = "group_by", columnDefinition = "json")
    @Type(type = "json")
    private GroupBy groupBy;

    @Column(name = "order_by", columnDefinition = "json")
    @Type(type = "json")
    private List<com.jingweizhang.dynaquery.model.OrderBy> orderBys;

    public DynaQuery(List<ProjectBy> projectBys, FilterCondition filterCondition, GroupBy groupBy, List<OrderBy> orderBys) {
        this.projectBys = projectBys;
        this.filterCondition = filterCondition;
        this.groupBy = groupBy;
        this.orderBys = orderBys;
    }
}

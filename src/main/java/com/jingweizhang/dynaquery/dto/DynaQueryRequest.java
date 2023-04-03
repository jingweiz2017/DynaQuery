package com.jingweizhang.dynaquery.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * VariantQueryRequest is used by the client to define a query at runtime.
 *
 * @Author jingwei.zhang on 2023/4/3
 */
@Data
public class DynaQueryRequest {
    private List<ProjectBy> projections;
    private FilterCondition filterCondition;
    private GroupBy group;
    private List<OrderBy> orders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectBy {
        private String field;
        private boolean isVisible;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes({ @JsonSubTypes.Type(UnaryFilterCondition.class), @JsonSubTypes.Type(BinaryFilterCondition.class) })
    public interface FilterCondition {}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BinaryFilterCondition implements FilterCondition {
        private FilterCondition left;
        private String connector;
        private FilterCondition right;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnaryFilterCondition implements FilterCondition {
        private List<FilterBy> filters;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterBy {
        private String field;
        private String operator;
        private List<String> values;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupBy {
        private List<String> fields;
        private Aggregator aggregator;
        private FilterCondition having;
        private String alias;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Aggregator {
            private String field;
            private String operator;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderBy {
        private String field;
        private String operator;
        private int sequence;
    }
}

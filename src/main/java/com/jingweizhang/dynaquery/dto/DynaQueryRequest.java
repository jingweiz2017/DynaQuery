package com.jingweizhang.dynaquery.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jingweizhang.dynaquery.model.FilterConnector;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * DynaQueryRequest is used by the client to define a query at runtime.
 *
 * @Author rocky.zhang on 2023/4/3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynaQueryRequest {
    private String targetView;
    private List<ProjectBy> projections;
    private Filter filter;
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
    @JsonSubTypes({ @JsonSubTypes.Type(CompositeFilter.class), @JsonSubTypes.Type(SimpleFilter.class) })
    public interface Filter {}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompositeFilter implements Filter {
        private List<Filter> filters;
        private FilterConnector connector;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleFilter implements Filter {
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
        private Filter having;
        private String alias;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class AggregatorFilter implements Filter {
            private Aggregator aggregator;
            private String operator;
            private List<String> values;
        }

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

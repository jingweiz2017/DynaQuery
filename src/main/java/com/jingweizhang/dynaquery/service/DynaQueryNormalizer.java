package com.jingweizhang.dynaquery.service;

import com.jingweizhang.dynaquery.dto.DynaQueryRequest;
import com.jingweizhang.dynaquery.exception.*;
import com.jingweizhang.dynaquery.extension.ViewEntity;
import com.jingweizhang.dynaquery.model.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description
 * User to validate the query and convert parameters for its data type
 *
 * @Author jingwei.zhang on 2023/4/3
 */
class DynaQueryNormalizer {
    private final Map<Class<?>, Map<String, Class<?>>> viewEntityDictionary;

    public DynaQueryNormalizer(String packageFullNamespace) {
        this.viewEntityDictionary = this.initializeViewEntityDictionary(packageFullNamespace);
    }

    private Map<Class<?>, Map<String, Class<?>>> initializeViewEntityDictionary(String packageFullNamespace) {
        Map<Class<?>, Map<String, Class<?>>> viewEntityDictionary = new HashMap<>();

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AssignableTypeFilter(ViewEntity.class));

            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(packageFullNamespace)) {
                Class<?> clazz = Class.forName(beanDefinition.getBeanClassName(), false, classLoader);
                viewEntityDictionary.put(clazz, this.extractEntityMetaData("", clazz, new HashMap<>()));
            }
        } catch (Exception ex) {
            throw new FailedToFindViewEntityClassException(ex);
        }

        return viewEntityDictionary;
    }

    private Map<String, Class<?>> extractEntityMetaData(String root, Class<?> clazz, Map<String, Class<?>> map) {
        for (Field field : clazz.getDeclaredFields()) {
            String path = root == null || root.isEmpty() ? field.getName() : root + '.' + field.getName();
            if (this.isBuiltInType(field.getType())) {
                map.put(path, field.getType());
            } else {
                this.extractEntityMetaData(path, field.getType(), map);
            }
        }

        return map;
    }

    private boolean isBuiltInType(Class<?> type) {
        return type.isPrimitive() || type.equals(Boolean.class) || type.equals(Byte.class) ||
                type.equals(Character.class) || type.equals(Short.class) || type.equals(Integer.class) ||
                type.equals(Long.class) || type.equals(Float.class) || type.equals(Double.class) ||
                type.equals(String.class) || type.equals(Instant.class) || type.isEnum();
    }

    public DynaQuery normalize(Class<?> viewEntityClazz, DynaQueryRequest dynaQueryRequest) {
        this.validate(viewEntityClazz, dynaQueryRequest);

        DynaQuery query = new DynaQuery();
        query.setProjectBys(this.normalizeProjectBy(viewEntityClazz, dynaQueryRequest.getProjections()));
        query.setFilterCondition(this.normalizeFilterCondition(viewEntityClazz, dynaQueryRequest.getFilterCondition()));
        query.setGroupBy(this.normalizeGroupBy(viewEntityClazz, dynaQueryRequest.getGroup()));
        query.setOrderBys(this.normalizeOrderBy(viewEntityClazz, dynaQueryRequest.getOrders()));

        return query;
    }

    private List<ProjectBy> normalizeProjectBy(Class<?> viewEntityClazz, List<DynaQueryRequest.ProjectBy> projectBys) {
        if (projectBys == null || projectBys.isEmpty()) {
            return Collections.emptyList();
        }

        return projectBys.stream().map((x) -> ProjectBy.of(x.getField(), x.isVisible())).collect(Collectors.toList());
    }

    /**
     * Validate the sourceFilterCondition of query request
     * @param viewEntityClazz
     *          view entity to validate against
     * @param sourceFilterCondition
     *          sourceFilterCondition to validate
     * @param syntheticFields
     *          synthetic fields are fields created by query itself rather than a field existed in view entity.
     *          Thus, there is no need to validate them.
     *          e.g. select sum(amount) as totalSum from order where id > 5.
     *          The field totalSum is a synthetic field created by query itself.
     * @return FilterCondition
     */
    private FilterCondition normalizeFilterCondition(Class<?> viewEntityClazz, DynaQueryRequest.FilterCondition sourceFilterCondition, List<String> syntheticFields) {
        if (sourceFilterCondition == null) return null;

        FilterCondition filterCondition;
        if (sourceFilterCondition instanceof DynaQueryRequest.UnaryFilterCondition) {
            DynaQueryRequest.UnaryFilterCondition unaryFilterCondition = (DynaQueryRequest.UnaryFilterCondition)sourceFilterCondition;

            List<FilterBy> filters = new ArrayList<>();

            for (DynaQueryRequest.FilterBy filter : unaryFilterCondition.getFilters()) {
                if (syntheticFields.contains(filter.getField())) continue;

                Map<String, Class<?>> entityMeta = this.viewEntityDictionary.get(viewEntityClazz);
                Class<?> fieldDataType = entityMeta.get(filter.getField());

                try {
                    List<Object> values = filter.getValues().stream().map(x -> this.convert(fieldDataType, x)).collect(Collectors.toList());
                    FilterBy filterBy = FilterBy.of(filter.getField(), FilterOperator.valueOf(filter.getOperator()), values);
                    filters.add(filterBy);
                } catch (Exception ex) {
                    throw new FailedToConvertFilterValuesToFieldDataTypeException(filter.getField(), fieldDataType.getName());
                }
            }

            filterCondition = new UnaryFilterCondition(filters);
        } else {
            DynaQueryRequest.BinaryFilterCondition binaryFilterCondition = (DynaQueryRequest.BinaryFilterCondition)sourceFilterCondition;
            FilterCondition left = this.normalizeFilterCondition(viewEntityClazz, binaryFilterCondition.getLeft(), syntheticFields);
            FilterCondition right = this.normalizeFilterCondition(viewEntityClazz, binaryFilterCondition.getRight(), syntheticFields);

            filterCondition = BinaryFilterCondition.of(left, FilterConditionConnector.valueOf(binaryFilterCondition.getConnector()),right);
        }

        return filterCondition;
    }

    private FilterCondition normalizeFilterCondition(Class<?> viewEntityClazz, DynaQueryRequest.FilterCondition sourceFilterCondition) {
        return this.normalizeFilterCondition(viewEntityClazz, sourceFilterCondition, Collections.emptyList());
    }

    private Object convert(Class<?> clazz, String value) {
        if (value == null) return null;

        if (clazz == Long.class) {
            return Long.parseLong(value);
        } else if (clazz == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (clazz == Byte.class) {
            return Byte.parseByte(value);
        } else if (clazz == Character.class) {
            return value.charAt(0);
        } else if (clazz == Short.class) {
            return Short.parseShort(value);
        } else if (clazz == Integer.class) {
            return Integer.parseInt(value);
        } else if (clazz == Float.class) {
            return Float.parseFloat(value);
        } else if (clazz == Double.class) {
            return Double.parseDouble(value);
        } else if (clazz == Instant.class) {
            return Instant.parse(value);
        } else if (Enum.class.isAssignableFrom(clazz)) {
            return Enum.valueOf((Class<Enum>) clazz, value);
        } else {
            return value;
        }
    }

    private GroupBy normalizeGroupBy(Class<?> viewEntityClazz, DynaQueryRequest.GroupBy groupBy) {
        return groupBy == null ? null :
                GroupBy.of(
                        groupBy.getFields(),
                        Aggregator.of(
                                groupBy.getAggregator().getField(),
                                AggregateOperator.valueOf(groupBy.getAggregator().getOperator())
                        ),
                        this.normalizeFilterCondition(viewEntityClazz, groupBy.getHaving(), Collections.singletonList(groupBy.getAlias())),
                        groupBy.getAlias()
                );
    }

    private List<OrderBy> normalizeOrderBy(Class<?> viewEntityClazz, List<DynaQueryRequest.OrderBy> orderBys) {
        List<OrderBy> orders = new ArrayList<>();
        if (orderBys == null || orderBys.isEmpty()) return orders;

        for (DynaQueryRequest.OrderBy orderBy : orderBys) {
            OrderBy order = OrderBy.of(orderBy.getField(), SortingOperator.valueOf(orderBy.getOperator()), orderBy.getSequence());
            orders.add(order);
        }

        return orders;
    }

    private void validate(Class<?> viewEntityClazz, DynaQueryRequest dynaQueryRequest) {
        this.validateFilterCondition(viewEntityClazz, dynaQueryRequest.getFilterCondition());
        this.validateGroupBy(viewEntityClazz, dynaQueryRequest.getGroup());
        this.validateOrderBys(viewEntityClazz, dynaQueryRequest.getOrders());
        this.validateProjectBys(viewEntityClazz, dynaQueryRequest.getProjections());
    }

    private void validateFilterCondition(Class<?> viewEntityClazz, DynaQueryRequest.FilterCondition filterCondition) {
        if (filterCondition == null) return;

        if (filterCondition instanceof DynaQueryRequest.UnaryFilterCondition) {
            for (DynaQueryRequest.FilterBy filter : ((DynaQueryRequest.UnaryFilterCondition) filterCondition).getFilters()) {
                this.validateFieldName(viewEntityClazz, filter.getField());

                try {
                    FilterOperator.valueOf(filter.getOperator());
                } catch (IllegalArgumentException ex) {
                    throw new UnsupportedFilterOperatorException(filter.getField(), filter.getOperator());
                }
            }
        } else {
            DynaQueryRequest.FilterCondition left = ((DynaQueryRequest.BinaryFilterCondition)filterCondition).getLeft();
            this.validateFilterCondition(viewEntityClazz, left);

            DynaQueryRequest.FilterCondition right = ((DynaQueryRequest.BinaryFilterCondition)filterCondition).getRight();
            this.validateFilterCondition(viewEntityClazz, right);
        }
    }

    private void validateGroupBy(Class<?> viewEntityClazz, DynaQueryRequest.GroupBy groupBy) {
        if (groupBy == null) return;

        groupBy.getFields().forEach(x -> this.validateFieldName(viewEntityClazz, x));

        AggregateOperator operator;
        try {
            operator = AggregateOperator.valueOf(groupBy.getAggregator().getOperator());
        } catch (IllegalArgumentException ex) {
            String message = "Unsupported aggregation operator %s on field %s.";
            throw new UnsupportedAggregateOperatorException(String.format(message, groupBy.getAggregator().getOperator(), groupBy.getAggregator().getField()));
        }

        if (groupBy.getAlias() == null || groupBy.getAlias().isEmpty()) {
            throw new InvalidFieldAliasException(groupBy.getAggregator().getField());
        }

        if (operator.equals(AggregateOperator.COUNT)) return;

        // Can't do aggregate on field with a data type other than number.
        if (!this.isNumber(this.viewEntityDictionary.get(viewEntityClazz).get(groupBy.getAggregator().getField()))) {
            String message = "Can't do aggregation on field %s with data type other than Number";
            throw new UnsupportedAggregateOperatorException(String.format(message, groupBy.getAggregator().getField()));
        }
    }

    private boolean isNumber(Class<?> clazz) {
        List<Class<?>> numericTypes = Arrays.asList(byte.class, short.class, int.class, long.class, float.class, double.class);
        return Number.class.isAssignableFrom(clazz) || numericTypes.contains(clazz);
    }

    private void validateOrderBys(Class<?> viewEntityClazz, List<DynaQueryRequest.OrderBy> orderBys) {
        if (orderBys == null || orderBys.isEmpty()) return;

        for (DynaQueryRequest.OrderBy order : orderBys) {
            this.validateFieldName(viewEntityClazz, order.getField());

            try {
                SortingOperator.valueOf(order.getOperator());
            } catch (IllegalArgumentException ex) {
                throw new UnsupportedSortingOperatorException(order.getField(), order.getOperator());
            }
        }
    }

    private void validateProjectBys(Class<?> viewEntityClazz, List<DynaQueryRequest.ProjectBy> projectBys) {
        if (projectBys == null || projectBys.isEmpty()) return;

        for (DynaQueryRequest.ProjectBy project : projectBys) {
            this.validateFieldName(viewEntityClazz, project.getField());
        }
    }

    private void validateFieldName(Class<?> viewEntityClazz, String fieldName) {
        if (!this.viewEntityDictionary.containsKey(viewEntityClazz)) {
            throw new FailedToFindViewEntityClassException(viewEntityClazz.getSimpleName());
        }

        if (!this.viewEntityDictionary.get(viewEntityClazz).containsKey(fieldName)) {
            throw new FailedToFindFieldInViewEntityClassException(viewEntityClazz.getSimpleName(), fieldName);
        }
    }
}

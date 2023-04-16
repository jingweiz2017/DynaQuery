package com.jingweizhang.dynaquery.service;

import com.jingweizhang.dynaquery.exception.DynaQueryGrammarException;
import com.jingweizhang.dynaquery.extension.ViewEntity;
import com.jingweizhang.dynaquery.model.*;
import com.jingweizhang.dynaquery.model.OrderBy;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Description
 * It will be responsible for executing DynaQuery with Criteria API.
 *
 * @Author rocky.zhang on 2023/4/3
 */
class DynaQueryExecutor {
    private final EntityManager entityManager;
    private final ViewEntityRegistry viewEntityRegistry;
    public DynaQueryExecutor(EntityManager entityManager, ViewEntityRegistry viewEntityRegistry) {
        this.entityManager = entityManager;
        this.viewEntityRegistry = viewEntityRegistry;
    }

    private <R> R doQueryOne(Class<R> returnClazz, DynaQuery dynaQuery) {
        Class<? extends ViewEntity> entityClazz = this.viewEntityRegistry.getViewEntityClass(dynaQuery.getTargetView());
        CriteriaQuery<R> contentQuery = CriteriaQueryConverter.of(this.entityManager.getCriteriaBuilder(), entityClazz).toContentQuery(dynaQuery, returnClazz);
        TypedQuery<R> query = entityManager.createQuery(contentQuery);
        return query.getSingleResult();
    }

    protected Optional<?> queryOneToEntity(DynaQuery dynaQuery) {
        Class<?> entityClazz = this.viewEntityRegistry.getViewEntityClass(dynaQuery.getTargetView());
        return Optional.of(this.doQueryOne(entityClazz, dynaQuery));
    }

    protected <T extends ViewEntity> Optional<Object[]> queryOneToRaw(DynaQuery dynaQuery) {
        return Optional.of(this.doQueryOne(Object[].class, dynaQuery));
    }

    protected <T extends ViewEntity> Optional<Map<String, Object>> queryOneToMap(DynaQuery dynaQuery) {
        return Optional.of(this.toMap(dynaQuery, this.doQueryOne(Object[].class, dynaQuery)));
    }

    public Optional<Map<String, Object>> queryOne(DynaQuery dynaQuery) {
        return dynaQuery.getGroupBy() != null ?
                this.queryOneToMap(dynaQuery) :
                this.queryOneToEntity(dynaQuery).map(entity -> this.toMap(dynaQuery, entity));
    }

    private <R> Page<R> doQueryAll(Class<R> resultClazz, DynaQuery dynaQuery, Pageable pageable) {
        Class<? extends ViewEntity> entityClazz = this.viewEntityRegistry.getViewEntityClass(dynaQuery.getTargetView());
        CriteriaQueryConverter<? extends ViewEntity> criteriaQueryConverter = CriteriaQueryConverter.of(this.entityManager.getCriteriaBuilder(), entityClazz);

        CriteriaQuery<R> contentQuery = criteriaQueryConverter.toContentQuery(dynaQuery, resultClazz);
        TypedQuery<R> contentTypeQuery = entityManager.createQuery(contentQuery).setFirstResult((int)pageable.getOffset()).setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = criteriaQueryConverter.toCountQuery(dynaQuery);
        TypedQuery<Long> countTypeQuery = entityManager.createQuery(countQuery);

        Page<R> page;
        try {
            List<R> contents = contentTypeQuery.getResultList();
            page = contents.isEmpty() ? Page.empty() : PageableExecutionUtils.getPage(contents, pageable, countTypeQuery::getSingleResult);
        } catch (PersistenceException e) {
            if (e.getCause() instanceof SQLGrammarException) {
                SQLGrammarException ex = (SQLGrammarException)e.getCause();
                throw new DynaQueryGrammarException(ex.getSQLState());
            }

            throw e;
        }

        return page;
    }

    // Return a list of entities as of entityClazz in parameter
    protected Page<?> queryAllToEntity(DynaQuery dynaQuery, Pageable pageable) {
        Class<? extends ViewEntity> entityClazz = this.viewEntityRegistry.getViewEntityClass(dynaQuery.getTargetView());
        return this.doQueryAll(entityClazz, dynaQuery, pageable);
    }

    // Return a list of raw object[] which requires the client to map each to its the field(column) it belongs to.
    protected Page<Object[]> queryAllToRaw(DynaQuery dynaQuery, Pageable pageable) {
        return this.doQueryAll(Object[].class, dynaQuery, pageable);
    }

    // Return a list of map which helps to identify field/column each value belongs to.
    protected Page<Map<String, Object>> queryAllToMap(DynaQuery dynaQuery, Pageable pageable) {
        return this.doQueryAll(Object[].class, dynaQuery, pageable).map(content -> this.toMap(dynaQuery, content));
    }

    // queryAll is the entrance of all query with more than one returns.
    public Page<Map<String, Object>> queryAll(DynaQuery dynaQuery, Pageable pageable) {
        return dynaQuery.getGroupBy() != null ?
            // For query with group by, queryAllToMap is the only supported Method.
            // JPA association is not yet supported in this case.
            this.queryAllToMap(dynaQuery, pageable) :
            // queryAllToEntity is the default method for queryAll when groupby is not presented.
            // For Entity with JPA association, queryAllToEntity is the only supported Method.
            this.queryAllToEntity(dynaQuery, pageable).map(entity -> this.toMap(dynaQuery, entity));
    }

    private <R> Map<String, Object> toMap(DynaQuery dynaQuery, R content) {
        List<String> fieldNames = new ArrayList<>();
        //if there is group by, then use group by fields. Projection over group by fields will make result seems wrong.
        if (dynaQuery.getGroupBy() != null) {
            fieldNames = dynaQuery.getGroupBy().getFields();
            fieldNames.add(dynaQuery.getGroupBy().getAlias());
        } else if (dynaQuery.getProjectBys() != null && !dynaQuery.getProjectBys().isEmpty()) {
            fieldNames = dynaQuery.getProjectBys().stream().map(ProjectBy::getField).collect(Collectors.toList());
        } else {
            List<Class<? extends Annotation>> tableAnnotations = Arrays.asList(Column.class, OneToMany.class, ManyToOne.class, OneToOne.class, ManyToMany.class);

            Class<?> entityClazz = this.viewEntityRegistry.getViewEntityClass(dynaQuery.getTargetView());
            for (Field field : entityClazz.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()) &&
                        tableAnnotations.stream().anyMatch(field::isAnnotationPresent)) {
                    fieldNames.add(field.getName());
                }
            }
        }

        Map<String, Object> record = new HashMap<>();
        for (int i = 0; i < fieldNames.size(); i++) {
            if (content instanceof Object[]) {
                record.put(fieldNames.get(i), ((Object[])content)[i]);
            } else if (ViewEntity.class.isAssignableFrom(content.getClass())) {
                record.put(fieldNames.get(i), getFieldValue(((ViewEntity)content).getClass(), fieldNames.get(i), content));
            } else {
                throw new RuntimeException("Not support type: " + content.getClass());
            }
        }

        return record;
    }

    public static <T extends ViewEntity> Object getFieldValue(Class<T> entityClazz, String fieldName, Object instance) {
        try {
            Method method = entityClazz.getDeclaredMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
            method.setAccessible(true);
            Object result = method.invoke(instance);

            return result instanceof Collection ? new ArrayList<>((Collection<?>) result) : result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @Description
     * CriteriaQueryConverter takes a DynaQuery and convert it to CriteriaQuery for execution
     *
     * @Author rocky.zhang on 2023/3/14
     */
    public static class CriteriaQueryConverter<E> {
        private Root<?> root;
        private final Class<?> entityClazz;
        private final CriteriaBuilder criteriaBuilder;
        public CriteriaQueryConverter(CriteriaBuilder criteriaBuilder, Class<E> entityClazz) {
            this.criteriaBuilder = criteriaBuilder;
            this.entityClazz = entityClazz;
        }

        public static <E> CriteriaQueryConverter<E> of(CriteriaBuilder criteriaBuilder, Class<E> entityClazz) {
            if (entityClazz == null) {
                throw new IllegalArgumentException("entityClazz must not be null");
            }

            return new CriteriaQueryConverter<>(criteriaBuilder, entityClazz);
        }

        public <R> CriteriaQuery<R> toContentQuery(DynaQuery dynaQuery, Class<R> resultClazz) {
            CriteriaQuery<R> criteriaQuery = this.initialQuery(resultClazz);

            this.filterBy(dynaQuery.getFilter(), criteriaQuery);
            this.groupBy(dynaQuery.getGroupBy(), criteriaQuery);
            this.orderBy(dynaQuery.getOrderBys(), criteriaQuery);
            this.projectBy(dynaQuery.getProjectBys(), criteriaQuery);

            return criteriaQuery;
        }

        public CriteriaQuery<Long> toCountQuery(DynaQuery dynaQuery) {
            CriteriaQuery<Long> criteriaQuery = this.initialQuery(Long.class);

            this.filterBy(dynaQuery.getFilter(), criteriaQuery);
            this.groupBy(dynaQuery.getGroupBy(), criteriaQuery);
            this.countBy(dynaQuery.getProjectBys(), criteriaQuery);

            return criteriaQuery;
        }

        private <R> CriteriaQuery<R> initialQuery(Class<R> resultClazz) {
            CriteriaQuery<R> criteriaQuery = this.criteriaBuilder.createQuery(resultClazz);
            this.root = criteriaQuery.from(this.entityClazz);
            this.root.getModel().getAttributes().forEach(x -> {
                if (x.isAssociation()) {
                    //TODO: maybe we can support groupby query result to support association with SingularAttribute?
                    if (Arrays.asList(Attribute.PersistentAttributeType.ONE_TO_MANY, Attribute.PersistentAttributeType.MANY_TO_MANY).contains(x.getPersistentAttributeType())) {
                        root.join(x.getName(), JoinType.LEFT);
                    } else if (Arrays.asList(Attribute.PersistentAttributeType.MANY_TO_ONE, Attribute.PersistentAttributeType.ONE_TO_ONE).contains(x.getPersistentAttributeType())) {
                        root.join(x.getName(), JoinType.INNER);
                    }
                }
            });

            return criteriaQuery.distinct(true);
        }

        private void countBy(List<ProjectBy> projectBys, CriteriaQuery<Long> criteriaQuery) {
            if (criteriaQuery.isDistinct()) {
                if (projectBys != null && !projectBys.isEmpty()) {
                    Expression<String> expression;
                    if (projectBys.size() == 1) {
                        expression = root.get(projectBys.get(0).getField());
                    } else {
                        expression = criteriaBuilder.concat(
                                root.get(projectBys.get(0).getField()),
                                root.get(projectBys.get(1).getField()));
                        for (int i = 2; i < projectBys.size(); i++) {
                            expression = criteriaBuilder.concat(expression, root.get(projectBys.get(i).getField()));
                        }
                    }
                    criteriaQuery.subquery(String.class).select(expression);
                }

                criteriaQuery.select(criteriaBuilder.countDistinct(root));
            } else {
                criteriaQuery.select(criteriaBuilder.count(root));
            }
        }

        private <R> void projectBy(List<ProjectBy> projectBys, CriteriaQuery<R> criteriaQuery) {
            // Project by is only valid when there is no group by
            if (criteriaQuery.getGroupList().isEmpty()) {
                criteriaQuery.select((Selection<R>) root);
            }
        }

        private <R> void doProjectBy(List<ProjectBy> projectBys, CriteriaQuery<R> criteriaQuery) {
            List<Selection<?>> projections = new ArrayList<>();
            for (ProjectBy project : projectBys) {
                projections.add(root.get(project.getField()).alias(project.getField()));
            }

            criteriaQuery.multiselect(projections);
        }

        private <R> void filterBy(Filter filter, CriteriaQuery<R> criteriaQuery) {
            if (filter != null) {
                Predicate predicate = this.doFilter(filter);
                criteriaQuery.where(predicate);
            }
        }

        private Predicate doFilter(Filter filter) {
            Predicate predicate;
            if (filter instanceof SimpleFilter) {
                predicate = this.doSimpleFilter((SimpleFilter) filter);
            } else if (filter instanceof CompositeFilter) {
                predicate = this.doCompositeFilter((CompositeFilter) filter);
            } else if (filter instanceof AggregatorFilter) {
                predicate = this.doAggregatorFilter((AggregatorFilter) filter);
            } else {
                throw new IllegalArgumentException("Unknown filter type: " + filter.getClass());
            }

            return predicate;
        }

        private Predicate doAggregatorFilter(AggregatorFilter aggregatorFilter) {
            Predicate predicate = null;
            if (aggregatorFilter != null) {
                Expression exp = this.getPath(root, aggregatorFilter.getAggregator().getField());

                switch (aggregatorFilter.getAggregator().getOperator()) {
                    case SUM:
                        exp = criteriaBuilder.sum(exp);
                        break;
                    case AVG:
                        exp = criteriaBuilder.avg(exp);
                        break;
                    case MIN:
                        exp = criteriaBuilder.min(exp);
                        break;
                    case MAX:
                        exp = criteriaBuilder.max(exp);
                        break;
                    case COUNT:
                        exp = criteriaBuilder.count(exp);
                        break;
                }
                predicate =  this.doExpressionFilter(exp, aggregatorFilter.getFilterOperator(), aggregatorFilter.getValues());
            }

            return predicate;
        }

        private Predicate doSimpleFilter(SimpleFilter simpleFilter) {
            Path<?> path = this.getPath(root, simpleFilter.getField());
            return this.doExpressionFilter(path, simpleFilter.getFilterOperator(), simpleFilter.getValues());
        }

        private Predicate doExpressionFilter(Expression exp, FilterOperator filterOperator, List<Object> values) {
            Predicate predicate;

            switch (filterOperator) {
                    case EQ:
                        predicate = criteriaBuilder.equal(exp, values.stream().findFirst().orElse(null));
                        break;
                    case NE:
                        predicate = criteriaBuilder.notEqual(exp, values.stream().findFirst().orElse(null));
                        break;
                    case LIKE:
                        predicate = criteriaBuilder.like(exp, this.addPercentSign(
                                values.stream().findFirst().orElse("").toString()));
                        break;
                    case NOTLIKE:
                        predicate = criteriaBuilder.notLike(exp, this.addPercentSign(
                                values.stream().findFirst().orElse("").toString()));
                        break;
                    case IN:
                        predicate = criteriaBuilder.and(exp.in(values.toArray()));
                        break;
                    case NOTIN:
                        predicate = criteriaBuilder.and(criteriaBuilder.not(exp.in(values.toArray())));
                        break;
                    case GT:
                        predicate = criteriaBuilder.greaterThan(exp,
                                (Comparable) values.stream().findFirst().orElse(null));
                        break;
                    case LT:
                        predicate = criteriaBuilder.lessThan(exp,
                                (Comparable) values.stream().findFirst().orElse(null));
                        break;
                    case GE:
                        predicate = criteriaBuilder.greaterThanOrEqualTo(
                                exp,
                                (Comparable) values.stream().findFirst().orElse(null));
                        break;
                    case LE:
                        predicate = criteriaBuilder.lessThanOrEqualTo(exp,
                                (Comparable) values.stream().findFirst().orElse(null));
                        break;
                    case ISNULL:
                        predicate = criteriaBuilder.isNull(exp);
                        break;
                    case NOTNULL:
                        predicate = criteriaBuilder.isNotNull(exp);
                        break;
                    case BETWEEN:
                        predicate = criteriaBuilder.between(exp,
                                (Comparable) values.get(0),
                                (Comparable) values.get(1));
                        break;
                    default:
                        throw new RuntimeException("Unsupported Filter Operator");
                }

            return predicate;
        }

        private Predicate doCompositeFilter(CompositeFilter compositeFilter) {
            List<Predicate> predicates = new ArrayList<>();

            if (compositeFilter != null) {
                for (Filter filter : compositeFilter.getFilters()) {
                    Predicate predicate;
                    if (filter instanceof SimpleFilter) {
                        predicate = this.doSimpleFilter((SimpleFilter) filter);
                    } else {
                        predicate = this.doCompositeFilter((CompositeFilter) filter);
                    }

                    if (predicate != null) {
                        predicates.add(predicate);
                    }
                }
            }

            return compositeFilter.getConnector() == FilterConnector.AND ?
                    criteriaBuilder.and(predicates.toArray(new Predicate[0])) :
                    criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        }

        private String addPercentSign(String param) {
            Pattern paramLike = Pattern.compile("(^%.*)|(.*%$)");
            return param == null ? param : paramLike.matcher(param).matches() ? param : "%" + param + "%";
        }

        private <R> void orderBy(List<OrderBy> orderBys, CriteriaQuery<R> criteriaQuery) {
            if (orderBys != null && !orderBys.isEmpty()) {
                orderBys.sort(Comparator.comparingInt(OrderBy::getSequence));
                List<Order> orders = new ArrayList<>();

                Order order;
                for (OrderBy orderBy : orderBys) {
                    order = orderBy.getOperator() == SortingOperator.ASC ?
                            this.criteriaBuilder.asc(this.getPath(root, orderBy.getField())) :
                            this.criteriaBuilder.desc(this.getPath(root, orderBy.getField()));

                    orders.add(order);
                }
                criteriaQuery.orderBy(orders);
            }
        }

        // Path like "student.name.firstName" is allowed
        // Given Entity defined as:
        // class Student {
        //     private Name name;
        // }
        // class Name {
        //     private String firstName;
        //     private String lastName;
        // }
        private Path<?> getPath(Root<?> root, String field) {
            String[] paths = field.split("\\.");

            Path<?> path = root;
            for (String p : paths) {
                path = path.get(p);
            }

            return path;
        }

        private <R> void groupBy(GroupBy groupBy, CriteriaQuery<R> criteriaQuery) {
            if (groupBy != null) {
                Selection<?> selection;
                Path<Number> fieldPath = root.get(groupBy.getAggregator().getField());
                switch (groupBy.getAggregator().getOperator()) {
                    case SUM:
                        selection = criteriaBuilder.sum(fieldPath).alias(groupBy.getAlias());
                        break;
                    case AVG:
                        selection = criteriaBuilder.avg(fieldPath).alias(groupBy.getAlias());
                        break;
                    case MAX:
                        selection = criteriaBuilder.max(fieldPath).alias(groupBy.getAlias());
                        break;
                    case MIN:
                        selection = criteriaBuilder.min(fieldPath).alias(groupBy.getAlias());
                        break;
                    case COUNT:
                        selection = criteriaBuilder.count(fieldPath).alias(groupBy.getAlias());
                        break;
                    default:
                        return;
                }

                List<Expression<?>> expressions = groupBy.getFields().stream().map(x->root.get(x)).collect(Collectors.toList());
                List<Selection<?>> selections = groupBy.getFields().stream().map(x->root.get(x).alias(x)).collect(Collectors.toList());
                selections.add(selection);

                criteriaQuery.multiselect(selections).groupBy(expressions);

                if (groupBy.getHaving() != null) {
                    criteriaQuery.having(this.doFilter(groupBy.getHaving()));
                }
            }
        }
    }
}

package com.jingweizhang.dynaquery.service;

import com.jingweizhang.dynaquery.dto.DynaQueryReference;
import com.jingweizhang.dynaquery.dto.DynaQueryRequest;
import com.jingweizhang.dynaquery.exception.FailedToFindDynaQueryException;
import com.jingweizhang.dynaquery.extension.ViewConverter;
import com.jingweizhang.dynaquery.extension.ViewEntity;
import com.jingweizhang.dynaquery.model.DynaQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Description
 * Handle all query request that require dynaquery ability.
 *
 * @Author jingwei.zhang on 2023/4/3
 */
@Service
public class DynaQueryService {
    private final DynaQueryExecutor dynaQueryExecutor;
    private final DynaQueryNormalizer dynaQueryNormalizer;
    private final DynaQueryRepository dynaQueryRepository;
    public DynaQueryService(EntityManager entityManager,
                            DynaQueryRepository dynaQueryRepository,
                            @Value("${dyna-query.view-entity-package}") String viewEntityPackage) {
        this.dynaQueryExecutor = new DynaQueryExecutor(entityManager);
        this.dynaQueryNormalizer = new DynaQueryNormalizer(viewEntityPackage);
        this.dynaQueryRepository = dynaQueryRepository;
    }

    @Transactional
    public <E extends ViewEntity> Optional<Map<String, Object>> queryOne(Class<E> entityClazz, DynaQueryRequest dynaQueryRequest, ViewConverter<Map<String, Object>, Map<String, Object>> resultConverter) {
        DynaQuery dynaQuery = this.dynaQueryNormalizer.normalize(entityClazz, dynaQueryRequest);
        return this.dynaQueryExecutor.queryOne(entityClazz, dynaQuery).map(resultConverter::convert);
    }

    @Transactional
    public <E extends ViewEntity> Page<Map<String, Object>> queryAll(Class<E> entityClazz, DynaQueryRequest dynaQueryRequest, Pageable pageable, ViewConverter<Map<String, Object>, Map<String, Object>> resultConverter) {
        DynaQuery dynaQuery = this.dynaQueryNormalizer.normalize(entityClazz, dynaQueryRequest);
        return this.dynaQueryExecutor.queryAll(entityClazz, dynaQuery, pageable).map(resultConverter::convert);
    }

    @Transactional
    public <E extends ViewEntity> Page<Map<String, Object>> querySavedQuery(Class<E> entityClazz, int id, Pageable pageable, ViewConverter<Map<String, Object>, Map<String, Object>> resultConverter) {
        Optional<DynaQuery> dynaQueryOptional = this.dynaQueryRepository.findById(id);

        if (!dynaQueryOptional.isPresent()) {
            throw new FailedToFindDynaQueryException();
        }

        DynaQuery dynaQuery = dynaQueryOptional.get();
        return this.dynaQueryExecutor.queryAll(entityClazz, dynaQuery, pageable).map(resultConverter::convert);
    }

    @Transactional
    public <E extends ViewEntity> DynaQueryReference saveQuery(Class<E> entityClazz, DynaQueryRequest dynaQueryRequest, String name, boolean isDefault) {
        DynaQuery dynaQuery = this.dynaQueryNormalizer.normalize(entityClazz , dynaQueryRequest);

        if (isDefault) {
            List<DynaQuery> queries = this.dynaQueryRepository.findAll();
            queries.forEach(x->x.setIsDefault(false));
            this.dynaQueryRepository.saveAll(queries);
        }

        dynaQuery.setName(name);
        dynaQuery.setIsDefault(isDefault);
        dynaQuery = this.dynaQueryRepository.save(dynaQuery);

        return DynaQueryReference.of(dynaQuery);
    }

    public List<DynaQueryReference> listVariantQueryReferences() {
        return this.dynaQueryRepository.findAll().stream().map(DynaQueryReference::of).collect(Collectors.toList());
    }
}

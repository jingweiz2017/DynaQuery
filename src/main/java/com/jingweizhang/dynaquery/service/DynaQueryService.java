package com.jingweizhang.dynaquery.service;

import com.jingweizhang.dynaquery.dto.DynaQueryReference;
import com.jingweizhang.dynaquery.dto.DynaQueryRequest;
import com.jingweizhang.dynaquery.exception.FailedToFindDynaQueryException;
import com.jingweizhang.dynaquery.extension.ViewConverter;
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
        ViewEntityRegistry viewEntityRegistry = new ViewEntityRegistry(viewEntityPackage);

        this.dynaQueryNormalizer = new DynaQueryNormalizer(viewEntityRegistry);
        this.dynaQueryExecutor = new DynaQueryExecutor(entityManager, viewEntityRegistry);
        this.dynaQueryRepository = dynaQueryRepository;
    }

    @Transactional
    public Optional<Map<String, Object>> queryOne(DynaQueryRequest dynaQueryRequest) {
        return this.queryOne(dynaQueryRequest, x->x);
    }

    @Transactional
    public Optional<Map<String, Object>> queryOne(DynaQueryRequest dynaQueryRequest, ViewConverter<Map<String, Object>, Map<String, Object>> resultConverter) {
        DynaQuery dynaQuery = this.dynaQueryNormalizer.normalize(dynaQueryRequest);
        return this.dynaQueryExecutor.queryOne(dynaQuery).map(resultConverter::convert);
    }

    @Transactional
    public Page<Map<String, Object>> queryAll(DynaQueryRequest dynaQueryRequest, Pageable pageable) {
        return this.queryAll(dynaQueryRequest, pageable, x->x);
    }

    @Transactional
    public Page<Map<String, Object>> queryAll(DynaQueryRequest dynaQueryRequest, Pageable pageable, ViewConverter<Map<String, Object>, Map<String, Object>> resultConverter) {
        DynaQuery dynaQuery = this.dynaQueryNormalizer.normalize(dynaQueryRequest);
        return this.dynaQueryExecutor.queryAll(dynaQuery, pageable).map(resultConverter::convert);
    }

    @Transactional
    public Page<Map<String, Object>> querySavedQuery(int id, Pageable pageable) {
        Optional<DynaQuery> dynaQueryOptional = this.dynaQueryRepository.findById(id);
        if (dynaQueryOptional.isEmpty()) {
            throw new FailedToFindDynaQueryException();
        }

        DynaQuery dynaQuery = dynaQueryOptional.get();
        return this.dynaQueryExecutor.queryAll(dynaQuery, pageable);
    }

    @Transactional
    public DynaQueryReference saveQuery(DynaQueryRequest dynaQueryRequest, String name, boolean isDefault) {
        DynaQuery dynaQuery = this.dynaQueryNormalizer.normalize(dynaQueryRequest);

        if (isDefault) {
            List<DynaQuery> queries = this.dynaQueryRepository.findAll();
            queries.forEach(x->x.setIsDefault(false));
            this.dynaQueryRepository.saveAll(queries);
        }

        dynaQuery.setName(name);
        dynaQuery.setIsDefault(isDefault);

        return DynaQueryReference.of(this.dynaQueryRepository.save(dynaQuery));
    }

    public List<DynaQueryReference> listDynaQueryReferences() {
        return this.dynaQueryRepository.findAll().stream().map(DynaQueryReference::of).collect(Collectors.toList());
    }
}

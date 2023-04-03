package com.jingweizhang.dynaquery.service;

import com.jingweizhang.dynaquery.model.DynaQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @Description
 * Access Variant Query entity in database
 *
 * @Author jingwei.zhang on 2023/4/3
 */
@Repository
public interface DynaQueryRepository extends JpaRepository<DynaQuery, Integer> {
}

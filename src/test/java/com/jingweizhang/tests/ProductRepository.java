package com.jingweizhang.tests;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @Description
 * @Author rocky.zhang on 2023/4/4
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
}

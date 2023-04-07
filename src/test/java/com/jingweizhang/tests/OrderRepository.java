package com.jingweizhang.tests;

/**
 * @Description
 * @Author rocky.zhang on 2023/4/4
 */
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    // define custom methods for accessing and manipulating Order entities
}


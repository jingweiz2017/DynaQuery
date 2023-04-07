package com.jingweizhang.dynaquery.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @Description
 * @Author rocky.zhang on 2023/4/7
 */
@Configuration
@EntityScan("com.jingweizhang.dynaquery")
@EnableJpaRepositories("com.jingweizhang.dynaquery")
public class DynaQueryAutoConfiguration {
}

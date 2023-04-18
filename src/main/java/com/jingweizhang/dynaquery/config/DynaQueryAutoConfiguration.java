package com.jingweizhang.dynaquery.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
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
    //region Swagger
    @Bean
    public GroupedOpenApi api(){
        return GroupedOpenApi.builder()
                .group("com.jingweizhang")
                .packagesToScan("com.jingweizhang.dynaquery.web")
                .build();
    }
    //endregion
}

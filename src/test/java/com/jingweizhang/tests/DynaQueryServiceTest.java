package com.jingweizhang.tests;

import com.jingweizhang.dynaquery.dto.DynaQueryRequest;
import com.jingweizhang.dynaquery.service.DynaQueryService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @Description
 * @Author rocky.zhang on 2023/3/27
 */
@SpringBootTest(
        properties = {
            "dyna-query.view-entity-package=com.jingweizhang.tests"
        }
)
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@ComponentScan(basePackages = {
        "com.jingweizhang.dynaquery"})
@EnableJpaRepositories(basePackageClasses = {
        OrderRepository.class
})
@EntityScan(basePackageClasses = {
        Order.class,
        Product.class
})
public class DynaQueryServiceTest {
    @Autowired
    private DynaQueryService variantQueryService;

    @Autowired
    private OrderRepository orderRepository;

    private static final int pageNum = 0;
    private static final int pageSize = 10;

    private List<Order> orders;

    @BeforeEach
    public void setUp() {
        Order entity1 = new Order();
        entity1.setCustomerName("customer");
        entity1.setAmount(6.5);
        entity1.setShippingAddress("5th Avenue, New York");
        entity1.setProducts(
                Arrays.asList(
                        new Product("product1", 1000),
                        new Product("product2", 500)
                )
        );

        Order entity2 = new Order();
        entity2.setCustomerName("customer1");
        entity2.setAmount(15.5);
        entity2.setShippingAddress("5th Avenue, New York");
        entity2.setProducts(
                Arrays.asList(
                        new Product("product1", 1000),
                        new Product("product2", 500)
                )
        );

        Order entity3 = new Order();
        entity3.setCustomerName("customer2");
        entity3.setAmount(11.6);
        entity3.setShippingAddress("5th Avenue, New York");
        entity3.setProducts(
                Arrays.asList(
                        new Product("product1", 1000),
                        new Product("product2", 500)
                )
        );

        this.orders = this.orderRepository.saveAll(Arrays.asList(entity1, entity2, entity3));
    }

    @AfterEach
    public void cleanUp() {
        this.orderRepository.deleteAll(this.orders);
    }

    @Test
    public void testFilterAndConditions() {
        DynaQueryRequest queryRequest = new DynaQueryRequest();
        queryRequest.setFilterCondition(
                new DynaQueryRequest.UnaryFilterCondition(
                        Arrays.asList(
                                new DynaQueryRequest.FilterBy(
                                        "amount",
                                        "EQ",
                                        Collections.singletonList("6.5")
                                ),
                                new DynaQueryRequest.FilterBy(
                                        "customerName",
                                        "EQ",
                                        Collections.singletonList("customer")
                                )
                        )
                )
        );

        Page<Map<String, Object>> result = variantQueryService.queryAll(Order.class, queryRequest, PageRequest.of(pageNum, pageSize), (x) -> x);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertTrue(result.getContent().get(0).get("amount").equals(6.5) && result.getContent().get(0).get("customerName").equals("customer"));
    }

    @Test
    public void testFilterOrConditions() {
        DynaQueryRequest queryRequest = new DynaQueryRequest();
        queryRequest.setFilterCondition(
                new DynaQueryRequest.BinaryFilterCondition(
                        new DynaQueryRequest.UnaryFilterCondition(
                                Collections.singletonList(
                                        new DynaQueryRequest.FilterBy(
                                                "customerName",
                                                "EQ",
                                                Collections.singletonList("customer")
                                        )
                                )
                        ),
                        "OR",
                        new DynaQueryRequest.UnaryFilterCondition(
                                Collections.singletonList(
                                        new DynaQueryRequest.FilterBy(
                                                "customerName",
                                                "EQ",
                                                Collections.singletonList("customer1")
                                        )
                                )
                        )
                )
        );

        Page<Map<String, Object>> result = this.variantQueryService.queryAll(Order.class, queryRequest, PageRequest.of(pageNum, pageSize), (x) -> x);

        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertFalse(result.getContent().stream().anyMatch(x -> !x.get("customerName").equals("customer") && !x.get("customerName").equals("customer1")));
    }

    @Test
    public void testOrderBy() throws Exception {
        String columnName = "amount";
        DynaQueryRequest queryRequest = new DynaQueryRequest();
        queryRequest.setOrders(
            Arrays.asList(
                new DynaQueryRequest.OrderBy(
                        columnName,
                        "ASC",
                        0
                )
            )
        );

        Page<Map<String, Object>> result = variantQueryService.queryAll(Order.class, queryRequest, PageRequest.of(pageNum, pageSize), (x) -> x);

        List<Map<String, Object>> sorted = result.getContent().stream().sorted(Comparator.comparing(x -> (Double)x.get(columnName))).collect(Collectors.toList());
        Assertions.assertArrayEquals(sorted.toArray(), result.getContent().toArray());
    }

    @Test
    public void testGroupBy() throws Exception {
        DynaQueryRequest queryRequest = new DynaQueryRequest();
        queryRequest.setGroup(
                new DynaQueryRequest.GroupBy(
                        new ArrayList<>(
                                Arrays.asList(
                                        "shippingAddress"
                                )
                        ),
                        new DynaQueryRequest.GroupBy.Aggregator(
                                "amount",
                                "SUM"
                        ),
                        null,
                        "totalSum"
                )
        );
        Page<Map<String, Object>> result = this.variantQueryService.queryAll(Order.class, queryRequest, PageRequest.of(pageNum, pageSize), (x) -> x);

        Assertions.assertEquals(33.6, (Double) result.getContent().get(0).get("totalSum"));
    }

    @Test
    public void testGroupByWithHaving() throws Exception {
        DynaQueryRequest queryRequest = new DynaQueryRequest();
        queryRequest.setGroup(
                new DynaQueryRequest.GroupBy(
                        new ArrayList<>(
                                Arrays.asList(
                                        "shippingAddress"
                                )
                        ),
                        new DynaQueryRequest.GroupBy.Aggregator(
                                "amount",
                                "SUM"
                        ),
                        new DynaQueryRequest.UnaryFilterCondition(
                                Arrays.asList(
                                        new DynaQueryRequest.FilterBy(
                                                "totalSum",
                                                "GT",
                                                Collections.singletonList("30")
                                        )
                                )
                        ),
                        "totalSum"
                )
        );

        Page<Map<String, Object>> result =
                this.variantQueryService.queryAll(Order.class, queryRequest, PageRequest.of(pageNum, pageSize), (x) -> x);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(33.6, (Double) result.getContent().get(0).get("totalSum"));
    }
}

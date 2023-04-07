package com.jingweizhang.tests;

/**
 * @Description
 * @Author rocky.zhang on 2023/4/4
 */

import com.jingweizhang.dynaquery.extension.ViewEntity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order implements ViewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private int orderId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Product> products;

    @Column(name = "amount")
    private double amount;

    public Order() {}

    public Order(String customerName, String shippingAddress, List<Product> products, double amount) {
        this.customerName = customerName;
        this.shippingAddress = shippingAddress;
        this.products = products;
        this.amount = amount;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}


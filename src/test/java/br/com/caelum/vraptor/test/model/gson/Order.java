package br.com.caelum.vraptor.test.model.gson;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private Long id;
    private Customer customer;
    private Address delivery;
    private List<Product> products;

    public Order() {
        this.products = new ArrayList<Product>();
    }

    public Order(Long id, Customer customer) {
        this();
        this.id = id;
    }

    public Order(Long id, Customer customer, Address delivery) {
        this();
        this.id = id;
        this.customer = customer;
        this.delivery = delivery;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void addProduct(Product product) {
        this.products.add(product);
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Address getDelivery() {
        return delivery;
    }

    public void setDelivery(Address delivery) {
        this.delivery = delivery;
    }
}

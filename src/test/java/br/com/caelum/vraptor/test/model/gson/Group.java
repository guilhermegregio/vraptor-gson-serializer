package br.com.caelum.vraptor.test.model.gson;

import java.util.List;

public class Group {

    private Long id;
    private String name;
    private List<Product> products;

    public Group() {
        super();
    }

    public Group(Long id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}

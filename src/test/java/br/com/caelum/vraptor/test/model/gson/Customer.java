package br.com.caelum.vraptor.test.model.gson;


public class Customer {
    private Long id;
    private String name;
    private Address address;

    public Customer(Long id, String name, Address address) {
        super();
        this.id = id;
        this.name = name;
        this.address = address;
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

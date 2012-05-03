package model;

import java.util.Date;

public class Product {
    private Long id;
    private String name;
    private Date creationDate;
    private Group group;
    private Object data;
    private byte[] image;

    public Product() {
        super();
    }

    public Product(Long id) {
        super();
        this.id = id;
    }

    public Product(Long id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public Product(Long id, String name, Date creationDate) {
        super();
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
    }

    public Product(Long id, String name, Date creationDate, Group group) {
        super();
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.group = group;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}

package model;

import java.util.Date;

public class HardDisk extends Product {
    private long capacity;

    public HardDisk() {
        super();
    }

    public HardDisk(Long id) {
        super(id);
    }

    public HardDisk(Long id, String name, long capacity) {
        super(id, name);
        this.setCapacity(capacity);
    }

    public HardDisk(Long id, String name, Date creationDate, long capacity) {
        super(id, name, creationDate);
        this.setCapacity(capacity);
    }

    public HardDisk(Long id, String name, Date creationDate, long capacity, Group group) {
        super(id, name, creationDate, group);
        this.setCapacity(capacity);
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }
}

package com.avaje.tests.model.basic;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class UUTwo {

    @Id
    UUID id;
    
    String name;

    @ManyToOne(cascade=CascadeType.PERSIST)
    UUOne master;
    
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUOne getMaster() {
        return master;
    }

    public void setMaster(UUOne master) {
        this.master = master;
    }
    
}

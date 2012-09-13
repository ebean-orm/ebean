package com.avaje.tests.model.basic;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class ENullCollection {

    @Id
    Integer id;
    
    @OneToMany(cascade=CascadeType.PERSIST)
    List<ENullCollectionDetail> details;
    
    public ENullCollection() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<ENullCollectionDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ENullCollectionDetail> details) {
        this.details = details;
    }
}

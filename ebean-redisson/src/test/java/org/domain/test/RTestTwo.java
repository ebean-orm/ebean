package org.domain.test;

import io.ebean.Model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class RTestTwo extends Model {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn
    private RTestOne testOne;

    public RTestTwo(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public RTestOne getTestOne() {
        return testOne;
    }

    public void setTestOne(RTestOne testOne) {
        this.testOne = testOne;
    }
}

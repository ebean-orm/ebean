package org.domain.test;

import io.ebean.Model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class TestTwo extends Model {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn
    private TestOne testOne;

    public TestTwo(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public TestOne getTestOne() {
        return testOne;
    }

    public void setTestOne(TestOne testOne) {
        this.testOne = testOne;
    }
}
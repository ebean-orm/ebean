package org.domain.test;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Cache
@Entity
public class TestOne extends Model {

    @Id
    private String id;

    @Column(unique = true)
    private String otherUnique;

    @OneToMany(mappedBy = "testOne", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestTwo> testTwos = new ArrayList<>();

    public TestOne(String id, String otherUnique) {
        this.id = id;
        this.otherUnique = otherUnique;
    }

    public String getId() {
        return id;
    }

    public String getOtherUnique() {
        return otherUnique;
    }

    public void setOtherUnique(String otherUnique) {
        this.otherUnique = otherUnique;
    }

    public List<TestTwo> getTestTwos() {
        return testTwos;
    }

    public void setTestTwos(List<TestTwo> testTwos) {
        this.testTwos = testTwos;
    }
}
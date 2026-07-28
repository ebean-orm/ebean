package org.domain.test;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Cache
@Entity
public class RTestOne extends Model {

    @Id
    private String id;

    @Column(unique = true)
    private String otherUnique;

    @OneToMany(mappedBy = "testOne", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RTestTwo> testTwos = new ArrayList<>();

    public RTestOne(String id, String otherUnique) {
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

    public List<RTestTwo> getTestTwos() {
        return testTwos;
    }

    public void setTestTwos(List<RTestTwo> testTwos) {
        this.testTwos = testTwos;
    }
}

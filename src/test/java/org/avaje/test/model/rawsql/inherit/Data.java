package org.avaje.test.model.rawsql.inherit;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rawinherit_data")
public class Data {

    @Id
    private Long id;

    private Integer number;

    @ManyToMany(mappedBy = "data")
    public List<Parent> parents = new ArrayList<Parent>();

    public Data(int number) {
        this.number = number;
    }

    public Long getId() {
        return id;
    }

    public Integer getNumber() {
        return number;
    }

}

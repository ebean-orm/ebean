package org.avaje.test.model.rawsql.inherit;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rawinherit_parent")
@Inheritance(strategy= InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type")
public abstract class Parent {

    @Id
    private Long id;

    private Integer number;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private List<Data> data = new ArrayList<Data>();

    protected Parent(Integer number) {
        this.number = number;
    }

    public abstract String getName();

    public Long getId() {
        return id;
    }

    public Integer getNumber() {
        return number;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> datas) {
        this.data = datas;
    }

}

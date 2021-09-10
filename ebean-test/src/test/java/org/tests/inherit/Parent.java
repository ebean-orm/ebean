package org.tests.inherit;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rawinherit_parent")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class Parent {

  @Id
  private Long id;

  private Integer val;

  private String more;

  @ManyToMany(cascade = CascadeType.ALL)
  private List<Data> data = new ArrayList<>();

  protected Parent(Integer val, String more) {
    this.val = val;
    this.more = more;
  }

  public abstract String getName();

  public Long getId() {
    return id;
  }

  public Integer getVal() {
    return val;
  }

  public List<Data> getData() {
    return data;
  }

  public void setData(List<Data> datas) {
    this.data = datas;
  }

  public String getMore() {
    return more;
  }

  public void setMore(String more) {
    this.more = more;
  }
}

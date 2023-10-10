package org.tests.inherit;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rawinherit_parent")
public final class ChildA {

  @Id
  private long id;

  private String type;

  private Integer val;

  private String more;

  @ManyToMany(cascade = CascadeType.ALL)
  private List<Data> data = new ArrayList<>();

  public ChildA(String type, Integer val, String more) {
    this.type = type;
    this.val = val;
    this.more = more;
  }

  public ChildA(int val, String more) {
    this.type = "B";
    this.val = val;
    this.more = more;
  }

  public String type() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

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

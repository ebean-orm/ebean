package org.tests.model.onetoone.calcd;

import io.ebean.Model;

import javax.persistence.*;

@Entity
@Table(name = "calcd_input")
public class CalcDInput extends Model {

  @Id
  private Integer id;

  @OneToOne(optional = false, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "input")
  private CalcDData data;

  private final String name;

  public CalcDInput(String name) {
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public CalcDData getData() {
    return data;
  }

  public void setData(CalcDData data) {
    this.data = data;
  }
}

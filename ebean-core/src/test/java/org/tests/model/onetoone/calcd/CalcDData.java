package org.tests.model.onetoone.calcd;

import io.ebean.Model;
import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.DbForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "calcd_data")
public class CalcDData extends Model {

  @Id
  private Integer id;

  @OneToOne(optional = false, cascade = CascadeType.ALL)
  @DbForeignKey(onDelete = ConstraintMode.CASCADE)
  @PrimaryKeyJoinColumn//(name = "Id", referencedColumnName = "Id")
  private CalcDInput input;

  private final String name;

  public CalcDData(String name) {
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public CalcDInput getInput() {
    return input;
  }

  public void setInput(CalcDInput input) {
    this.input = input;
  }

  public String getName() {
    return name;
  }
}

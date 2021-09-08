package org.tests.model.basic;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "non_updateprop")
public class MNonUpdPropEntity {

  @Id
  Integer id;

  @Enumerated(EnumType.STRING)
  MNonEnum nonEnum;

  @Column(updatable = false)
  String name;

  String note;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public MNonEnum getNonEnum() {
    return nonEnum;
  }

  public void setNonEnum(MNonEnum nonEnum) {
    this.nonEnum = nonEnum;
  }

}

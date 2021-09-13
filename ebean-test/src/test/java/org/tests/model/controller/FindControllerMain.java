package org.tests.model.controller;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class FindControllerMain {

  @Id
  private Integer id;

  private Integer targetId;

  private String targetTableName;

  @Transient
  private Object target;

  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public Integer getTargetId() {
    return targetId;
  }

  public void setTargetId(final Integer targetId) {
    this.targetId = targetId;
  }

  public String getTargetTableName() {
    return targetTableName;
  }

  public void setTargetTableName(final String targetTableName) {
    this.targetTableName = targetTableName;
  }

  public Object getTarget() {
    return target;
  }

  public void setTarget(final Object target) {
    this.target = target;
  }
}

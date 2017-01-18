package org.tests.it.ddlgeneration;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import io.ebean.annotation.EnumValue;
import io.ebean.annotation.Index;

@Entity
public class ModelUnderTestA {

  public enum Status {
    @EnumValue("N")
    NEW,

    @EnumValue("A")
    ACTIVE,

    @EnumValue("I")
    INACTIVE,
  }

  @Id
  Integer id;

  Status status;

  @Index
  String name;

  String description;

  Timestamp someDate;
  
  @ManyToOne
  GlobalTestModel testModel;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Timestamp getSomeDate() {
    return someDate;
  }

  public void setSomeDate(Timestamp someDate) {
    this.someDate = someDate;
  }

  public GlobalTestModel getTestModel() {
    return testModel;
  }
  
  public void setTestModel(GlobalTestModel testModel) {
    this.testModel = testModel;
  }
}

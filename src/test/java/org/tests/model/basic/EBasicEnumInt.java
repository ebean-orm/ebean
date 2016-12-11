package org.tests.model.basic;

import io.ebean.annotation.DbEnumType;
import io.ebean.annotation.DbEnumValue;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "e_basic_eni")
public class EBasicEnumInt {

  public enum Status {
    NEW("1"),
    ACTIVE("2"),
    INACTIVE("3");

    String value;

    Status(String value) {
      this.value = value;
    }

    @DbEnumValue(storage = DbEnumType.INTEGER)
    public String getValue() {
      return value;
    }
  }

  @Id
  Integer id;

  Status status;

  String name;

  String description;

  Timestamp someDate;

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

}

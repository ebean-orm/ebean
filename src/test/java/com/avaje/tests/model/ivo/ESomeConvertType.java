package com.avaje.tests.model.ivo;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ESomeConvertType {

  @Id
  Long id;

  String name;

  Rate rate;

  public ESomeConvertType(String name, Rate rate) {
    this.name = name;
    this.rate = rate;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Rate getRate() {
    return rate;
  }

  public void setRate(Rate rate) {
    this.rate = rate;
  }
}

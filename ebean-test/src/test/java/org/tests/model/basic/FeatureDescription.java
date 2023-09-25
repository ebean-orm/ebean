package org.tests.model.basic;

import io.ebean.annotation.Cache;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Cache(readOnly = true)
@Entity
@Table(name = "feature_desc", indexes = {
  @Index(columnList = "name, description desc"),
  @Index(columnList = "name desc")})
public class FeatureDescription {

  @Id
  private Integer id;

  private String name;

  private String description;

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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}

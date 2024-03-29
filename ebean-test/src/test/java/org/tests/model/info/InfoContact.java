package org.tests.model.info;

import io.ebean.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;

@Entity
public class InfoContact extends Model {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @ManyToOne(optional = false)
  InfoCompany company;

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

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public InfoCompany getCompany() {
    return company;
  }

  public void setCompany(InfoCompany company) {
    this.company = company;
  }
}

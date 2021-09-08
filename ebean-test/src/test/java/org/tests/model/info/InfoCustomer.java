package org.tests.model.info;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class InfoCustomer extends Model {

  public static final Finder<Long, InfoCustomer> find = new Finder<>(InfoCustomer.class);

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @OneToOne(cascade = CascadeType.ALL)
  InfoCompany company;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public InfoCompany getCompany() {
    return company;
  }

  public void setCompany(InfoCompany company) {
    this.company = company;
  }
}

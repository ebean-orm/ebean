package org.tests.model.info;

import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.List;

@Entity
public class InfoCompany extends Model {

  public static final Finder<Long, InfoCompany> find = new Finder<>(InfoCompany.class);

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @JsonIgnore
  @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
  List<InfoContact> contacts = new ArrayList<>();

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

  public List<InfoContact> getContacts() {
    return contacts;
  }

  public void setContacts(List<InfoContact> contacts) {
    this.contacts = contacts;
  }
}

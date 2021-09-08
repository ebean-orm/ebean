package org.tests.model.elementcollection;

import io.ebean.annotation.Cache;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.List;

@Cache
@Entity
public class EcPerson {

  @Id
  long id;

  String name;

  @ElementCollection
  @CollectionTable(name = "ec_person_phone", joinColumns = @JoinColumn(name = "owner_id", referencedColumnName = "id"))
  @Column(name = "phone")
  List<String> phoneNumbers = new ArrayList<>();

  @Version
  long version;

  public EcPerson(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "person id:" + id + " name:" + name + " phs:" + phoneNumbers;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getPhoneNumbers() {
    return phoneNumbers;
  }

  public void setPhoneNumbers(List<String> phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}

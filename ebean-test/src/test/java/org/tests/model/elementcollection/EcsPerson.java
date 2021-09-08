package org.tests.model.elementcollection;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
public class EcsPerson {

  @Id
  long id;

  String name;

  @ElementCollection
  @CollectionTable(name = "ecs_person_phone")
  @Column(name = "phone")
  Set<String> phoneNumbers = new LinkedHashSet<>();

  @Version
  long version;

  public EcsPerson(String name) {
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

  public Set<String> getPhoneNumbers() {
    return phoneNumbers;
  }

  public void setPhoneNumbers(Set<String> phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}

package org.tests.model.elementcollection;

import io.ebean.annotation.Cache;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Version;
import java.util.LinkedHashMap;
import java.util.Map;

@Cache
@Entity
public class EcbmPerson {

  @Id
  long id;

  String name;

  @ElementCollection
  @CollectionTable(joinColumns = @JoinColumn(name = "person_id"))
  Map<String,EcPhone> phoneNumbers = new LinkedHashMap<>();

  @Version
  long version;

  public EcbmPerson(String name) {
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

  public Map<String,EcPhone> getPhoneNumbers() {
    return phoneNumbers;
  }

  public void setPhoneNumbers(Map<String,EcPhone> phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}

package org.tests.model.elementcollection;

import io.ebean.annotation.Cache;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.Map;

@Cache
@Entity
public class EcmcPerson {

  @Id
  long id;

  String name;

  @ElementCollection
  @MapKeyColumn(name = "type", length = 4)
  @Lob
  Map<String, String> phoneNumbers = new LinkedHashMap<>();

  @Version
  long version;

  public EcmcPerson(String name) {
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

  public Map<String, String> getPhoneNumbers() {
    return phoneNumbers;
  }

  public void setPhoneNumbers(Map<String, String> phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}

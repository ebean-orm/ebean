package org.tests.model.array;


import io.ebean.annotation.DbArray;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
public class EArraySetBean {

  @Id
  Long id;

  String name;

  @DbArray(length = 300)
  Set<String> phoneNumbers = new LinkedHashSet<>();

  @DbArray
  Set<UUID> uids = new LinkedHashSet<>();

  @DbArray
  Set<Long> otherIds = new LinkedHashSet<>();

  @DbArray
  Set<Double> doubs;

  @Version
  Long version;

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

  public Set<String> getPhoneNumbers() {
    return phoneNumbers;
  }

  public void setPhoneNumbers(Set<String> phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
  }

  public Set<UUID> getUids() {
    return uids;
  }

  public void setUids(Set<UUID> uids) {
    this.uids = uids;
  }

  public Set<Long> getOtherIds() {
    return otherIds;
  }

  public void setOtherIds(Set<Long> otherIds) {
    this.otherIds = otherIds;
  }

  public Set<Double> getDoubs() {
    return doubs;
  }

  public void setDoubs(Set<Double> doubs) {
    this.doubs = doubs;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}

package com.avaje.tests.model.array;


import com.avaje.ebean.annotation.DbArray;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class EArrayBean {

  @Id
  Long id;

  String name;

  @DbArray(length = 300)
  List<String> phoneNumbers = new ArrayList<String>();

  @DbArray
  List<UUID> uids = new ArrayList<UUID>();

  @DbArray
  List<Long> otherIds = new ArrayList<Long>();

  @DbArray
  List<Double> doubs;

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

  public List<String> getPhoneNumbers() {
    return phoneNumbers;
  }

  public void setPhoneNumbers(List<String> phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
  }

  public List<UUID> getUids() {
    return uids;
  }

  public void setUids(List<UUID> uids) {
    this.uids = uids;
  }

  public List<Long> getOtherIds() {
    return otherIds;
  }

  public void setOtherIds(List<Long> otherIds) {
    this.otherIds = otherIds;
  }

  public List<Double> getDoubs() {
    return doubs;
  }

  public void setDoubs(List<Double> doubs) {
    this.doubs = doubs;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}

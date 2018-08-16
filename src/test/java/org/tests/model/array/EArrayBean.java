package org.tests.model.array;


import io.ebean.annotation.DbArray;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
public class EArrayBean {

  enum Status {
    ONE, TWO, THREE
  }

  IntEnum foo;

  @Id
  Long id;

  String name;

  @DbArray(length = 300)
  List<String> phoneNumbers = new ArrayList<>();

  @DbArray
  List<UUID> uids = new ArrayList<>();

  @DbArray
  List<Long> otherIds = new ArrayList<>();

  @DbArray
  List<Double> doubs;

  @DbArray
  List<Status> statuses;

  @DbArray
  List<VarcharEnum> vcEnums = new ArrayList<>();

  @DbArray
  List<IntEnum> intEnums = new ArrayList<>();

  @DbArray
  Set<Status> status2;

  @Version
  Long version;

  public IntEnum getFoo() {
    return foo;
  }

  public void setFoo(final IntEnum foo) {
    this.foo = foo;
  }

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

  public List<Status> getStatuses() {
    return statuses;
  }

  public void setStatuses(List<Status> statuses) {
    this.statuses = statuses;
  }

  public List<VarcharEnum> getVcEnums() {
    return vcEnums;
  }

  public void setVcEnums(final List<VarcharEnum> vcEnums) {
    this.vcEnums = vcEnums;
  }

  public List<IntEnum> getIntEnums() {
    return intEnums;
  }

  public void setIntEnums(final List<IntEnum> intEnums) {
    this.intEnums = intEnums;
  }

  public Set<Status> getStatus2() {
    return status2;
  }

  public void setStatus2(Set<Status> status2) {
    this.status2 = status2;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}

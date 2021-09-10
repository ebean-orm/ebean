package org.tests.model.json;

import io.ebean.annotation.DbJson;
import io.ebean.annotation.DbJsonB;
import io.ebean.annotation.DbJsonType;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.*;

import static io.ebean.annotation.MutationDetection.HASH;
import static io.ebean.annotation.MutationDetection.SOURCE;

@Entity
public class EBasicJsonList {

  @Id
  Long id;

  String name;

  // @JsonDeserialize(as=LinkedHashSet.class)
  @DbJson(length = 700, name = "beans")
  Set<PlainBean> beanSet;

  @DbJsonB(mutationDetection = HASH)
  List<PlainBean> beanList;

  @DbJson(length = 700)
  Map<String, PlainBean> beanMap = new LinkedHashMap<>();

  @DbJson(length = 500, mutationDetection = SOURCE) // such that we can rebuild old values
  PlainBean plainBean;

  @DbJson(length = 50)
  Set<Long> flags = new LinkedHashSet<>();

  @DbJson(length = 100, storage = DbJsonType.VARCHAR)
  List<String> tags = new ArrayList<>();

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

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public Set<Long> getFlags() {
    return flags;
  }

  public void setFlags(Set<Long> flags) {
    this.flags = flags;
  }

  public PlainBean getPlainBean() {
    return plainBean;
  }

  public void setPlainBean(PlainBean plainBean) {
    this.plainBean = plainBean;
  }

  public Set<PlainBean> getBeanSet() {
    return beanSet;
  }

  public void setBeanSet(Set<PlainBean> beanSet) {
    this.beanSet = beanSet;
  }

  public List<PlainBean> getBeanList() {
    return beanList;
  }

  public void setBeanList(List<PlainBean> beanList) {
    this.beanList = beanList;
  }

  public Map<String, PlainBean> getBeanMap() {
    return beanMap;
  }

  public void setBeanMap(Map<String, PlainBean> beanMap) {
    this.beanMap = beanMap;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}

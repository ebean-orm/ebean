package org.tests.model.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.DbJsonB;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
public class EBasicJsonJackson {

  @Id
  Long id;

  String name;

  @DbJson(length = 700)
  @JsonDeserialize(contentAs = String.class)
  Set<Object> valueSet = new LinkedHashSet<>();

  @DbJsonB
  @JsonDeserialize(contentAs = String.class)
  List<Object> valueList = new ArrayList<>();

  @DbJson(length = 700)
  @JsonDeserialize(keyAs = Long.class, contentAs = String.class)
  Map<Object, Object> valueMap = new LinkedHashMap<>();

  @DbJson(length = 500)
  @JsonDeserialize(as = String.class)
  Object plainValue;

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

  public Set<Object> getValueSet() {
    return valueSet;
  }

  public void setValueSet(Set<Object> valueSet) {
    this.valueSet = valueSet;
  }

  public List<Object> getValueList() {
    return valueList;
  }

  public void setValueList(List<Object> valueList) {
    this.valueList = valueList;
  }

  public Object getPlainValue() {
    return plainValue;
  }

  public void setPlainValue(Object plainValue) {
    this.plainValue = plainValue;
  }

  public Map<Object, Object> getValueMap() {
    return valueMap;
  }

  public void setValueMap(Map<Object, Object> valueMap) {
    this.valueMap = valueMap;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}

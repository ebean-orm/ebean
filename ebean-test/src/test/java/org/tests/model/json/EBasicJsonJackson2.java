package org.tests.model.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.DbJsonB;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.*;

@Entity
public class EBasicJsonJackson2 {

  @Id
  Long id;

  String name;

  @DbJson(length = 700)
  Set<BasicJacksonType<?>> valueSet = new LinkedHashSet<>();

  @DbJsonB
  List<BasicJacksonType<?>> valueList = new ArrayList<>();

  @DbJson(length = 700)
  @JsonDeserialize(keyAs = Long.class)
  Map<Number, BasicJacksonType<?>> valueMap = new LinkedHashMap<>();

  @DbJson(length = 500)
  BasicJacksonType<?> plainValue;

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

  public Set<BasicJacksonType<?>> getValueSet() {
    return valueSet;
  }

  public void setValueSet(Set<BasicJacksonType<?>> valueSet) {
    this.valueSet = valueSet;
  }

  public List<BasicJacksonType<?>> getValueList() {
    return valueList;
  }

  public void setValueList(List<BasicJacksonType<?>> valueList) {
    this.valueList = valueList;
  }

  public BasicJacksonType<?> getPlainValue() {
    return plainValue;
  }

  public void setPlainValue(BasicJacksonType<?> plainValue) {
    this.plainValue = plainValue;
  }

  public Map<Number, BasicJacksonType<?>> getValueMap() {
    return valueMap;
  }

  public void setValueMap(Map<Number, BasicJacksonType<?>> valueMap) {
    this.valueMap = valueMap;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}

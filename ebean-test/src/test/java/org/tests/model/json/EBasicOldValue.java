package org.tests.model.json;

import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.annotation.DbArray;
import io.ebean.annotation.DbJson;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.*;

import static io.ebean.annotation.MutationDetection.SOURCE;

@Entity
public class EBasicOldValue {

  @Id
  Long id;

  String name;

  @DbJson(mutationDetection = SOURCE)
  Set<String> stringSet = new LinkedHashSet<>();

  @DbJson(mutationDetection = SOURCE)
  Set<Long> longSet = new LinkedHashSet<>();

  @DbJson(mutationDetection = SOURCE)
  Set<Integer> intSet = new LinkedHashSet<>();

  @DbJson(mutationDetection = SOURCE)
  List<String> stringList = new ArrayList<>();

  @DbJson(mutationDetection = SOURCE)
  List<Long> longList = new ArrayList<>();

  @DbJson(mutationDetection = SOURCE)
  List<Integer> intList = new ArrayList<>();

  @DbJson(mutationDetection = SOURCE)
  Map<String, Object> objectMap = new LinkedHashMap<>();

  @DbJson(mutationDetection = SOURCE)
  Map<String, Long> longMap = new LinkedHashMap<>();

  @DbJson(mutationDetection = SOURCE)
  Map<String, Integer> intMap = new LinkedHashMap<>();

  @DbJson(mutationDetection = SOURCE)
  JsonNode jsonNode;

  @DbArray()
  List<String> stringArr = new ArrayList<>();


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

  public Set<String> getStringSet() {
    return stringSet;
  }

  public void setStringSet(Set<String> stringSet) {
    this.stringSet = stringSet;
  }

  public Set<Long> getLongSet() {
    return longSet;
  }

  public void setLongSet(Set<Long> longSet) {
    this.longSet = longSet;
  }

  public Set<Integer> getIntSet() {
    return intSet;
  }

  public void setIntSet(Set<Integer> intSet) {
    this.intSet = intSet;
  }

  public List<String> getStringList() {
    return stringList;
  }

  public void setStringList(List<String> stringList) {
    this.stringList = stringList;
  }

  public List<Long> getLongList() {
    return longList;
  }

  public void setLongList(List<Long> longList) {
    this.longList = longList;
  }

  public List<Integer> getIntList() {
    return intList;
  }

  public void setIntList(List<Integer> intList) {
    this.intList = intList;
  }

  public Map<String, Object> getObjectMap() {
    return objectMap;
  }

  public void setObjectMap(Map<String, Object> objectMap) {
    this.objectMap = objectMap;
  }

  public Map<String, Long> getLongMap() {
    return longMap;
  }

  public void setLongMap(Map<String, Long> longMap) {
    this.longMap = longMap;
  }

  public Map<String, Integer> getIntMap() {
    return intMap;
  }

  public void setIntMap(Map<String, Integer> intMap) {
    this.intMap = intMap;
  }

  public JsonNode getJsonNode() {
    return jsonNode;
  }

  public void setJsonNode(JsonNode jsonNode) {
    this.jsonNode = jsonNode;
  }

  public List<String> getStringArr() {
    return stringArr;
  }

  public void setStringArr(List<String> stringArr) {
    this.stringArr = stringArr;
  }

}

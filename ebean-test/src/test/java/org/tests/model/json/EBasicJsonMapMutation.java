package org.tests.model.json;

import io.ebean.annotation.DbJson;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.util.Map;

import static io.ebean.annotation.MutationDetection.HASH;
import static io.ebean.annotation.MutationDetection.NONE;
import static io.ebean.annotation.MutationDetection.SOURCE;

/**
 * Entity with {@code Map<String,Object>} @DbJson properties covering each
 * {@code MutationDetection} mode - used to verify that the built-in Map JSON
 * type honours mutationDetection rather than always using ModifyAware checking.
 */
@Entity
public class EBasicJsonMapMutation {

  @Id
  Long id;

  @DbJson
  Map<String, Object> defaultMap;

  @DbJson(mutationDetection = NONE)
  Map<String, Object> noneMap;

  @DbJson(mutationDetection = HASH)
  Map<String, Object> hashMap;

  @DbJson(mutationDetection = SOURCE)
  Map<String, Object> sourceMap;

  @Version
  Long version;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Map<String, Object> getDefaultMap() {
    return defaultMap;
  }

  public void setDefaultMap(Map<String, Object> defaultMap) {
    this.defaultMap = defaultMap;
  }

  public Map<String, Object> getNoneMap() {
    return noneMap;
  }

  public void setNoneMap(Map<String, Object> noneMap) {
    this.noneMap = noneMap;
  }

  public Map<String, Object> getHashMap() {
    return hashMap;
  }

  public void setHashMap(Map<String, Object> hashMap) {
    this.hashMap = hashMap;
  }

  public Map<String, Object> getSourceMap() {
    return sourceMap;
  }

  public void setSourceMap(Map<String, Object> sourceMap) {
    this.sourceMap = sourceMap;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}

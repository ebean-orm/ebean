package org.tests.model.elementcollection;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Version;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
public class EcsmTwo {

  @Id
  UUID id;

  String name;

  @ElementCollection
  @CollectionTable(name = "ecsm_values", joinColumns = @JoinColumn(name = "host_id", referencedColumnName = "id"))
  Set<String> values = new LinkedHashSet<>();

  @Version
  long version;

  public EcsmTwo(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "id:" + id + " name:" + name + " values:" + values;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public Set<String> getValues() {
    return values;
  }

  public void setValues(Set<String> values) {
    this.values = values;
  }
}

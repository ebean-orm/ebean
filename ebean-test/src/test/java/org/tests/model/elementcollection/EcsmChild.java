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
public class EcsmChild {

  @Id
  UUID oneId;

  String name;

  @ElementCollection
  @CollectionTable(name = "ecsm_values", joinColumns = @JoinColumn(name = "host_id", referencedColumnName = "one_id"))
  Set<String> values = new LinkedHashSet<>();

  @Version
  long version;

  public EcsmChild(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "id:" + oneId + " name:" + name + " values:" + values;
  }

  public UUID getOneId() {
    return oneId;
  }

  public void setOneId(UUID oneId) {
    this.oneId = oneId;
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

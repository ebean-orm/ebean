package org.tests.model.history;

import io.ebean.Model;
import io.ebean.annotation.History;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Version;

@Entity
@History
public class HEmbiBean extends Model {

  @EmbeddedId
  HEmbiId id;

  String name;
  String description;
  @Version
  long version;

  public HEmbiBean(HEmbiId id, String name) {
    this.id = id;
    this.name = name;
  }

  public HEmbiId getId() {
    return id;
  }

  public void setId(HEmbiId id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}

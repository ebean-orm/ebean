package org.tests.model.orphanremoval;

import io.ebean.annotation.Cache;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import java.util.List;

@Cache
@Entity
public class OrpMaster {

  @Id
  @Size(max = 100)
  String id;

  String name;

  @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "master")
  List<OrpDetail> details;

  @Version
  long version;

  public OrpMaster(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<OrpDetail> getDetails() {
    return details;
  }

  public void setDetails(List<OrpDetail> details) {
    this.details = details;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}

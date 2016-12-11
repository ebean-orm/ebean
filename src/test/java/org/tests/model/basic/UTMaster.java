package org.tests.model.basic;

import io.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ut_master")
public class UTMaster extends Model {

  @Id
  Integer id;

  String name;

  String description;

  @Version
  Integer version;

  @OneToMany(cascade = CascadeType.ALL)
  List<UTDetail> details;

  public UTMaster() {

  }

  public UTMaster(String name) {
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
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

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public List<UTDetail> getDetails() {
    return details;
  }

  public void setDetails(List<UTDetail> details) {
    this.details = details;
  }

  public void addDetail(UTDetail detail) {
    if (details == null) {
      details = new ArrayList<>();
    }
    details.add(detail);
  }
}

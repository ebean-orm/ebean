package org.tests.model.onetoone;

import io.ebean.annotation.Where;

import javax.persistence.*;
import java.util.List;

@Entity
public class OtoMasterVersion {

  @Id
  Long id;

  String name;

  @OneToOne(cascade = CascadeType.ALL, mappedBy = "master")
  OtoChildVersion child;

  @Version
  int version;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "ref_id")
  @Where(clause = "${mta}.type=0")
  List<OtoNotification> notifications;

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

  public OtoChildVersion getChild() {
    return child;
  }

  public void setChild(OtoChildVersion child) {
    this.child = child;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

}

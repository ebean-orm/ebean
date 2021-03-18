package org.tests.model.basic;

import io.ebean.DB;
import io.ebean.annotation.PostSoftDelete;
import io.ebean.annotation.PreSoftDelete;
import io.ebean.annotation.SoftDelete;

import javax.annotation.PostConstruct;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "e_basic_withlog")
public class EBasicWithLog {

  @Id
  Long id;

  String name;

  @SoftDelete
  boolean deleted;

  @Version
  Long version;

  @PrePersist
  public void prePersist() {
    writeLog("prePersist");
  }

  @PostPersist
  public void postPersist() {
    writeLog("postPersist");
  }

  @PreUpdate
  public void preUpdate() {
    writeLog("preUpdate");
  }

  @PostUpdate
  public void postUpdate() {
    writeLog("postUpdate");
  }

  @PreRemove
  public void preRemove() {
    writeLog("preRemove");
  }

  @PostRemove
  public void postRemove() {
    writeLog("postRemove");
  }

  @PostSoftDelete
  public void postSoftDelete() {
    writeLog("postSoftDelete");
  }

  @PreSoftDelete
  public void preSoftDelete() {
    writeLog("preSoftDelete");
  }

  @PostLoad
  public void postLoad() {
    writeLog("postLoad");
  }

  @PostConstruct
  public void postConstruct() {
    writeLog("postConstruct");
  }

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

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  private void writeLog(String title) {
    EBasicLog log = new EBasicLog(name);
    log.setName(title);
    DB.save(log);
  }

}

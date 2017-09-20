package org.tests.model.basic;

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
@Table(name = "e_basic_withlife")
public class EBasicWithLifecycle {

  @Id
  Long id;

  String name;

  @SoftDelete
  boolean deleted;

  @Version
  Long version;

  transient StringBuilder buffer;

  @PrePersist
  public void prePersist1() {
    getBuffer().append("prePersist1");
  }

  @PrePersist
  public void prePersist2() {
    getBuffer().append("prePersist2");
  }

  @PostPersist
  public void postPersist1() {
    getBuffer().append("postPersist1");
  }

  @PostPersist
  public void postPersist2() {
    getBuffer().append("postPersist2");
  }

  @PreUpdate
  public void preUpdate1() {
    getBuffer().append("preUpdate1");
  }

  @PreUpdate
  public void preUpdate2() {
    getBuffer().append("preUpdate2");
  }

  @PostUpdate
  public void postUpdate1() {
    getBuffer().append("postUpdate1");
  }

  @PostUpdate
  public void postUpdate2() {
    getBuffer().append("postUpdate2");
  }

  @PreRemove
  public void preRemove1() {
    getBuffer().append("preRemove1");
  }

  @PreRemove
  public void preRemove2() {
    getBuffer().append("preRemove2");
  }

  @PostRemove
  public void postRemove1() {
    getBuffer().append("postRemove1");
  }

  @PostRemove
  public void postRemove2() {
    getBuffer().append("postRemove2");
  }

  @PostSoftDelete
  public void postSoftDelete() {
    getBuffer().append("postSoftDelete");
  }

  @PreSoftDelete
  public void preSoftDelete() {
    getBuffer().append("preSoftDelete");
  }

  @PostLoad
  public void postLoad1() {
    getBuffer().append("postLoad1");
  }

  @PostLoad
  public void postLoad2() {
    getBuffer().append("postLoad2");
  }

  @PostConstruct
  public void postConstruct1() {
    getBuffer().append("postConstruct1");
  }

  @PostConstruct
  public void postConstruct2() {
    getBuffer().append("postConstruct2");
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

  public StringBuilder getBuffer() {
    if (buffer == null) {
      buffer = new StringBuilder();
    }
    return buffer;
  }

}

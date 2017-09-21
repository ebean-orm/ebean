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

  transient StringBuilder buffer = new StringBuilder();

  @PrePersist
  public void prePersist1() {
    buffer.append("prePersist1");
  }

  @PrePersist
  public void prePersist2() {
    buffer.append("prePersist2");
  }

  @PostPersist
  public void postPersist1() {
    buffer.append("postPersist1");
  }

  @PostPersist
  public void postPersist2() {
    buffer.append("postPersist2");
  }

  @PreUpdate
  public void preUpdate1() {
    buffer.append("preUpdate1");
  }

  @PreUpdate
  public void preUpdate2() {
    buffer.append("preUpdate2");
  }

  @PostUpdate
  public void postUpdate1() {
    buffer.append("postUpdate1");
  }

  @PostUpdate
  public void postUpdate2() {
    buffer.append("postUpdate2");
  }

  @PreRemove
  public void preRemove1() {
    buffer.append("preRemove1");
  }

  @PreRemove
  public void preRemove2() {
    buffer.append("preRemove2");
  }

  @PostRemove
  public void postRemove1() {
    buffer.append("postRemove1");
  }

  @PostRemove
  public void postRemove2() {
    buffer.append("postRemove2");
  }

  @PostSoftDelete
  public void postSoftDelete() {
    buffer.append("postSoftDelete");
  }

  @PreSoftDelete
  public void preSoftDelete() {
    buffer.append("preSoftDelete");
  }

  @PostLoad
  public void postLoad1() {
    buffer.append("postLoad1");
  }

  @PostLoad
  public void postLoad2() {
    buffer.append("postLoad2");
  }

  @PostConstruct
  public void postConstruct1() {
    buffer.append("postConstruct1");
  }

  @PostConstruct
  public void postConstruct2() {
    buffer.append("postConstruct2");
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

  public String getBuffer() {
    return buffer.toString();
  }

}

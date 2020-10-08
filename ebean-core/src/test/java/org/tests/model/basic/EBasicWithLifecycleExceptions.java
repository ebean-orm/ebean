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
@Table(name = "e_basic_with_ex")
public class EBasicWithLifecycleExceptions {

  @Id
  Long id;

  @SoftDelete
  boolean deleted;

  @Version
  Long version;

  public transient Throwable preException;
  public transient Throwable postException;

  @PrePersist
  public void prePersist() throws Throwable {
    if (preException != null) {
      throw preException;
    }
  }

  @PostPersist
  public void postPersist() throws Throwable {
    if (postException != null) {
      throw postException;
    }
  }

  @PreUpdate
  public void preUpdate() throws Throwable {
    if (preException != null) {
      throw preException;
    }
  }

  @PostUpdate
  public void postUpdate() throws Throwable {
    if (postException != null) {
      throw postException;
    }
  }

  @PreRemove
  public void preRemove() throws Throwable {
    if (preException != null) {
      throw preException;
    }
  }

  @PostRemove
  public void postRemove() throws Throwable {
    if (postException != null) {
      throw postException;
    }
  }

  @PreSoftDelete
  public void preSoftDelete() throws Throwable {
    if (preException != null) {
      throw preException;
    }
  }

  @PostSoftDelete
  public void postSoftDelete() throws Throwable {
    if (postException != null) {
      throw postException;
    }
  }

  @PostLoad
  public void postLoad1() throws Throwable {
    if (postException != null) {
      throw postException;
    }
  }

  @PostConstruct
  public void postConstruct1() throws Throwable {
    if (postException != null) {
      throw postException;
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

}

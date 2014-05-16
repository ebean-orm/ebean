package com.avaje.tests.model.basic;

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
@Table(name="e_basic_withlife")
public class EBasicWithLifecycle {

  @Id
  Long id;
  
  String name;
  
  @Version
  Long version;
  
  transient StringBuilder buffer = new StringBuilder();
  
  @PrePersist
  public void prePersist() {
    buffer.append("prePersist,");
  }
  
  @PostPersist
  public void postPersist() {
    buffer.append("postPersist,");
  }
  
  @PreUpdate
  public void preUpdate() {
    buffer.append("preUpdate,");
  }
  
  @PostUpdate
  public void postUpdate() {
    buffer.append("postUpdate,");
  }

  @PreRemove
  public void preRemove() {
    buffer.append("preRemove,");
  }
  
  @PostRemove
  public void postRemove() {
    buffer.append("postRemove");
  }
  
  @PostLoad
  public void postLoad() {
    buffer.append("postLoad");
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

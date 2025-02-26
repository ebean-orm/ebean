package org.tests.model.basic;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import io.ebean.annotation.Identity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

import static io.ebean.annotation.IdentityGenerated.BY_DEFAULT;

@Cache(enableQueryCache = true)
@Entity
@Table(name = "e_basicver")
public class EBasicVer extends Model {

  @Id @Identity(generated = BY_DEFAULT)
  Integer id;

  String name;

  String description;

  String other;

  @Version
  Instant lastUpdate;

  public EBasicVer(String name) {
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

  public String getOther() {
    return other;
  }

  public void setOther(String other) {
    this.other = other;
  }

  public Instant getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(Instant lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

}

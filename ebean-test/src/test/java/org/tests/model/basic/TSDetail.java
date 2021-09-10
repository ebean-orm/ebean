package org.tests.model.basic;

import io.ebean.annotation.Index;
import io.ebean.annotation.Platform;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * A basic entity to test simple things.
 */
@Index(name = "ix_t_detail_with_other_namexxxyy_lowername", unique = true, columnNames = "lower(name)", concurrent = true, platforms = Platform.POSTGRES)
@Index(name = "ix_t_detail_with_other_namexxxyy_defn", platforms = Platform.POSTGRES, definition = "create index ix_t_detail_with_other_namexxxyy_defn on t_detail_with_other_namexxxyy using hash (lower(name)) where lower(name) like 'r%'")
@Entity
@Table(name = "t_detail_with_other_namexxxyy")
public class TSDetail {


  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "t_atable_detail_seq")
  @Id
  Integer id;

  String name;

  String description;

  @Index(unique = true)
  @Size(max=127)
  String someUniqueValue;

  boolean active;

  @ManyToOne
  TSMaster master;

  public TSDetail(String name) {
    this.name = name;
  }

  public TSDetail(String name, String someUniqueValue) {
    this.name = name;
    this.someUniqueValue = someUniqueValue;
  }

  public TSDetail() {

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

  public String getSomeUniqueValue() {
    return someUniqueValue;
  }

  public void setSomeUniqueValue(String someUniqueValue) {
    this.someUniqueValue = someUniqueValue;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public TSMaster getMaster() {
    return master;
  }

  public void setMaster(TSMaster master) {
    this.master = master;
  }
}

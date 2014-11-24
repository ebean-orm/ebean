package com.avaje.tests.model;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Entity
public class EGenProps {

  @Id
  Long id;

  String name;

  @Version
  Long version;

  @CreatedTimestamp
  Timestamp tsCreated;

  @UpdatedTimestamp
  Timestamp tsUpdated;

  @CreatedTimestamp
  LocalDateTime ldtCreated;

  @UpdatedTimestamp
  LocalDateTime ldtUpdated;

  @CreatedTimestamp
  OffsetDateTime odtCreated;

  @UpdatedTimestamp
  OffsetDateTime odtUpdated;

  @CreatedTimestamp
  ZonedDateTime zdtCreated;

  @UpdatedTimestamp
  ZonedDateTime zdtUpdated;

  @CreatedTimestamp
  long longCreated;

  @UpdatedTimestamp
  long longUpdated;

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

  public Timestamp getTsCreated() {
    return tsCreated;
  }

  public void setTsCreated(Timestamp tsCreated) {
    this.tsCreated = tsCreated;
  }

  public Timestamp getTsUpdated() {
    return tsUpdated;
  }

  public void setTsUpdated(Timestamp tsUpdated) {
    this.tsUpdated = tsUpdated;
  }

  public LocalDateTime getLdtCreated() {
    return ldtCreated;
  }

  public void setLdtCreated(LocalDateTime ldtCreated) {
    this.ldtCreated = ldtCreated;
  }

  public LocalDateTime getLdtUpdated() {
    return ldtUpdated;
  }

  public void setLdtUpdated(LocalDateTime ldtUpdated) {
    this.ldtUpdated = ldtUpdated;
  }

  public OffsetDateTime getOdtCreated() {
    return odtCreated;
  }

  public void setOdtCreated(OffsetDateTime odtCreated) {
    this.odtCreated = odtCreated;
  }

  public OffsetDateTime getOdtUpdated() {
    return odtUpdated;
  }

  public void setOdtUpdated(OffsetDateTime odtUpdated) {
    this.odtUpdated = odtUpdated;
  }

  public ZonedDateTime getZdtCreated() {
    return zdtCreated;
  }

  public void setZdtCreated(ZonedDateTime zdtCreated) {
    this.zdtCreated = zdtCreated;
  }

  public ZonedDateTime getZdtUpdated() {
    return zdtUpdated;
  }

  public void setZdtUpdated(ZonedDateTime zdtUpdated) {
    this.zdtUpdated = zdtUpdated;
  }

  public long getLongCreated() {
    return longCreated;
  }

  public void setLongCreated(long longCreated) {
    this.longCreated = longCreated;
  }

  public long getLongUpdated() {
    return longUpdated;
  }

  public void setLongUpdated(long longUpdated) {
    this.longUpdated = longUpdated;
  }
}

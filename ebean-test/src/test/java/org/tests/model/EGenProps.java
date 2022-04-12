package org.tests.model;

import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.sql.Timestamp;
import java.time.Instant;
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

  @WhenCreated
  Timestamp whenCreated;

  @WhenModified
  Timestamp whenModified;

  @WhenCreated
  Timestamp tsCreated;

  @WhenModified
  Timestamp tsUpdated;

  @WhenCreated
  LocalDateTime ldtCreated;

  @WhenModified
  LocalDateTime ldtUpdated;

  @WhenCreated
  OffsetDateTime odtCreated;

  @WhenModified
  OffsetDateTime odtUpdated;

  @WhenCreated
  ZonedDateTime zdtCreated;

  @WhenModified
  ZonedDateTime zdtUpdated;

  @WhenCreated
  Instant instantCreated;

  @WhenModified
  Instant instantUpdated;

  @WhenCreated
  long longCreated;

  @WhenModified
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

  public Timestamp getWhenCreated() {
    return whenCreated;
  }

  public void setWhenCreated(Timestamp whenCreated) {
    this.whenCreated = whenCreated;
  }

  public Timestamp getWhenModified() {
    return whenModified;
  }

  public void setWhenModified(Timestamp whenModified) {
    this.whenModified = whenModified;
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

  public Instant getInstantCreated() {
    return instantCreated;
  }

  public void setInstantCreated(Instant instantCreated) {
    this.instantCreated = instantCreated;
  }

  public Instant getInstantUpdated() {
    return instantUpdated;
  }

  public void setInstantUpdated(Instant instantUpdated) {
    this.instantUpdated = instantUpdated;
  }
}

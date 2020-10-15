package org.example.domain;

import java.time.Instant;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;

@MappedSuperclass
abstract class BaseModel : Model() {
  @Id
  val id: Long? = null

  @Version
  val version: Int = 0

  @WhenCreated
  val whenCreated: Instant? = null

  @WhenModified
  val whenModified: Instant? = null
}

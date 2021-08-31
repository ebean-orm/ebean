package org.example.domain;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.example.otherpackage.GenericType;
import org.example.otherpackage.GenericTypeArgument;

/**
 * Address entity bean.
 */
@Entity
@Table(name = "o_address")
class Address(
  @Size(max = 100)
  var line1: String,
  @Size(max = 100)
  var line2: String,
  @Size(max = 100)
  var city: String,
  // Dummy metadata field just to test generation
  val metadata: GenericType<GenericTypeArgument>,
  @Transient
  val javaxPersistenceTransient: Set<String>,
  @kotlin.jvm.Transient
  val kotlinJvmTransient: Set<String>,
) : BaseModel()

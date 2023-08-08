package org.example.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * Country entity bean.
 */
@Entity
@Table(name = "o_country")
public class Country {

  @Id
  String code;
  String name;
  Instant whenCreated;
  OffsetDateTime offsetTimestamp;

  public Country(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public String toString() {
    return code;
  }

  /**
   * Return code.
   */
  public String getCode() {
    return code;
  }

  /**
   * Set code.
   */
  public void setCode(String code) {
    this.code = code;
  }

  /**
   * Return name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set name.
   */
  public void setName(String name) {
    this.name = name;
  }


}

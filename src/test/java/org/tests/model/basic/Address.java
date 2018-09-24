package org.tests.model.basic;

import io.ebean.annotation.InvalidateQueryCache;
import org.tests.model.basic.metaannotation.SizeMedium;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

/**
 * Address entity bean.
 */
// Address is not L2 cached directly but it is joined to queries that are cached
// What InvalidateQueryCache means is that we propagate a table modification event
// when address is changed ... and cached queries that join to address will be
// invalidated accordingly.
@InvalidateQueryCache
@Entity
@Table(name = "o_address")
public class Address {

  @Id
  Short id;

  @Size(max = 100)
  @Column(name = "line_1")
  String line1;

  @SizeMedium
  @Column(name = "line_2")
  String line2;

  @SizeMedium
  String city;

  Timestamp cretime;

  @Version
  Timestamp updtime;

  @ManyToOne
  Country country;


  @Override
  public String toString() {
    return id + " " + line1 + " " + line2 + " " + city + " " + country;
  }

  /**
   * Return id.
   */
  public Short getId() {
    return id;
  }

  /**
   * Set id.
   */
  public void setId(Short id) {
    this.id = id;
  }

  /**
   * Return line 1.
   */
  public String getLine1() {
    return line1;
  }

  /**
   * Set line 1.
   */
  public void setLine1(String line1) {
    this.line1 = line1;
  }

  /**
   * Return line 2.
   */
  public String getLine2() {
    return line2;
  }

  /**
   * Set line 2.
   */
  public void setLine2(String line2) {
    this.line2 = line2;
  }

  /**
   * Return city.
   */
  public String getCity() {
    return city;
  }

  /**
   * Set city.
   */
  public void setCity(String city) {
    this.city = city;
  }

  /**
   * Return cretime.
   */
  public Timestamp getCretime() {
    return cretime;
  }

  /**
   * Set cretime.
   */
  public void setCretime(Timestamp cretime) {
    this.cretime = cretime;
  }

  /**
   * Return updtime.
   */
  public Timestamp getUpdtime() {
    return updtime;
  }

  /**
   * Set updtime.
   */
  public void setUpdtime(Timestamp updtime) {
    this.updtime = updtime;
  }

  /**
   * Return country.
   */
  public Country getCountry() {
    return country;
  }

  /**
   * Set country.
   */
  public void setCountry(Country country) {
    this.country = country;
  }

}

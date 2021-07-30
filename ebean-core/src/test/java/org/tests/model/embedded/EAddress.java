package org.tests.model.embedded;

import io.ebean.annotation.DbJson;
import org.tests.model.json.PlainBean;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Map;

@Embeddable
public class EAddress {

  @Column(nullable = false)
  String street;

  String suburb;

  String city;

  @Enumerated(EnumType.STRING)
  EAddressStatus status;

  @DbJson
  PlainBean jbean;

  @DbJson
  Map<String, Object> jraw;

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getSuburb() {
    return suburb;
  }

  public void setSuburb(String suburb) {
    this.suburb = suburb;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public PlainBean getJbean() {
    return jbean;
  }

  public void setJbean(PlainBean jbean) {
    this.jbean = jbean;
  }

  public Map<String, Object> getJraw() {
    return jraw;
  }

  public void setJraw(Map<String, Object> jraw) {
    this.jraw = jraw;
  }

  public EAddressStatus getStatus() {
    return status;
  }

  public void setStatus(EAddressStatus status) {
    this.status = status;
  }
}

package org.tests.model.json;

import java.sql.Timestamp;

/**
 * Something for Jackson ObjectMapper to marshall.
 */
public class PlainBean {

  String name;

  long along;

  Timestamp timestamp;

  public PlainBean(String name, long along) {
    this.name = name;
    this.along = along;
    this.timestamp = new Timestamp(System.currentTimeMillis());
  }

  /**
   * A constructor for Jackson.
   */
  public PlainBean() {
  }

  @Override
  public String toString() {
    return "name:" + name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getAlong() {
    return along;
  }

  public void setAlong(long along) {
    this.along = along;
  }

  public Timestamp getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Timestamp timestamp) {
    this.timestamp = timestamp;
  }
}

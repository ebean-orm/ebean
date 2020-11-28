package org.tests.model.json;

import io.ebean.ModifyAwareType;

import java.sql.Timestamp;

/**
 * Something for Jackson ObjectMapper to marshall that is ModifyAwareType.
 */
public class PlainBeanDirtyAware implements ModifyAwareType {

  private transient boolean markedDirty;

  String name;

  long along;

  Timestamp timestamp;

  public PlainBeanDirtyAware(String name, long along) {
    this.name = name;
    this.along = along;
    this.timestamp = new Timestamp(System.currentTimeMillis());
  }

  /**
   * A constructor for Jackson.
   */
  public PlainBeanDirtyAware() {
  }

//  @JsonIgnore
  @Override
  public boolean isMarkedDirty() {
    return markedDirty;
  }

  @Override
  public void setMarkedDirty(boolean markedDirty) {
    this.markedDirty = markedDirty;
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

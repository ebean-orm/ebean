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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (along ^ (along >>> 32));
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PlainBean other = (PlainBean) obj;
    if (along != other.along)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (timestamp == null) {
      if (other.timestamp != null)
        return false;
    } else if (!timestamp.equals(other.timestamp))
      return false;
    return true;
  }
}

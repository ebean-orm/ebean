package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;

/**
 * Fixed / given location. Used internally for find by id and find all.
 */
final class BasicProfileLocation implements ProfileLocation {

  private final String fullLocation;
  private final String location;
  private final String label;

  BasicProfileLocation(String fullLocation) {
    this.fullLocation = fullLocation;
    this.location = shortDesc(fullLocation);
    this.label = UtilLocation.label(location);
  }

  @Override
  public String toString() {
    return location;
  }

  @Override
  public void add(long executionTime) {
    // do nothing
  }

  @Override
  public boolean obtain() {
    return false;
  }

  @Override
  public String label() {
    return label;
  }

  @Override
  public String location() {
    return location;
  }

  @Override
  public String fullLocation() {
    return fullLocation;
  }

  @Override
  public boolean trace() {
    return false;
  }

  @Override
  public void setTraceCount(int traceCount) {
    // do nothing
  }

  private String shortDesc(String location) {
    int lastPer = location.lastIndexOf('.');
    if (lastPer > -1) {
      lastPer = location.lastIndexOf('.', lastPer - 1);
      if (lastPer > -1) {
        return location.substring(lastPer + 1);
      }
    }
    return location;
  }

}

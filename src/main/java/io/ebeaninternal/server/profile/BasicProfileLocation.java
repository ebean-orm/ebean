package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;

/**
 * Fixed / given location. Used internally for find by id and find all.
 */
final class BasicProfileLocation implements ProfileLocation {

  private final String location;
  private final String shortDescription;
  private final String label;

  BasicProfileLocation(String location) {
    this.location = location;
    this.shortDescription = shortDesc(location);
    this.label = UtilLocation.label(shortDescription);
  }

  @Override
  public String toString() {
    return shortDescription;
  }

  @Override
  public void add(long executionTime) {
    // do nothing
  }

  @Override
  public String obtain() {
    return location;
  }

  @Override
  public String label() {
    return label;
  }

  @Override
  public String shortDescription() {
    return shortDescription;
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

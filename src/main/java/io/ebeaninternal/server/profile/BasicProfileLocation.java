package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;

/**
 * Fixed / given location. Used internally for find by id and find all.
 */
class BasicProfileLocation implements ProfileLocation {

  private final String location;

  BasicProfileLocation(String location) {
    this.location = location;
  }

  public String toString() {
    return "location: " + location;
  }

  public String obtain() {
    return location;
  }

}

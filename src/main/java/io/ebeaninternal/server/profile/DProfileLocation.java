package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;

/**
 * Default profile location that uses stack trace.
 */
class DProfileLocation implements ProfileLocation {

  private static final String IO_EBEAN = "io.ebean";

  private static final String UNKNOWN = "unknown";

  private String location;

  private String shortDescription;

  private final int lineNumber;

  DProfileLocation() {
    this(0);
  }

  /**
   * Create with a given line number.
   */
  DProfileLocation(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  @Override
  public String toString() {
    return "location: " + location;
  }

  @Override
  public void add(long executionTime) {
    // do nothing
  }

  @Override
  public String obtain() {
    // atomic assignment so happy with this
    if (location == null) {
      location = create();
      shortDescription = shortDesc(location);
    }
    return location;
  }

  @Override
  public String shortDescription() {
    return shortDescription;
  }

  private String create() {
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    for (int i = 3; i < trace.length; i++) {
      if (!trace[i].getClassName().startsWith(IO_EBEAN)) {
        return withLineNumber(trace[i].toString());
      }
    }
    return UNKNOWN;
  }

  private String withLineNumber(String traceLine) {
    if (lineNumber == 0) {
      return traceLine;
    } else if (traceLine.endsWith(":1)")) {
      return traceLine.substring(0, traceLine.length() - 3) + ":" + lineNumber + ")";
    } else if (traceLine.contains(":")) {
      return traceLine;
    } else {
      return traceLine.substring(0, traceLine.length() - 1) + ":" + lineNumber + ")";
    }
  }

  private String shortDesc(String location) {
    int pos = location.lastIndexOf('(');
    if (pos == -1) {
      pos = location.length();
    }

    pos = location.lastIndexOf('.', pos);
    if (pos > -1) {
      pos = location.lastIndexOf('.', pos - 1);
      if (pos > -1) {
        return location.substring(pos + 1);
      }
    }
    return location;
  }
}

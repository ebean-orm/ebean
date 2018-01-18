package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;

/**
 * Default profile location that uses stack trace.
 */
class DProfileLocation implements ProfileLocation {

  private static final String IO_EBEAN = "io.ebean";

  private static final String UNKNOWN = "unknown";

  private String location;

  private final int lineNumber;

  DProfileLocation() {
    this.lineNumber = 0;
  }

  DProfileLocation(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public String toString() {
    return "location: " + location;
  }

  public String obtain() {
    // atomic assignment so happy with this
    if (location == null) {
      location = create();
    }
    return location;
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
    if (lineNumber == 0 || traceLine.contains(":")) {
      return traceLine;
    } else {
      return traceLine.substring(0, traceLine.length() - 1) + ":" + lineNumber + ")";
    }
  }
}

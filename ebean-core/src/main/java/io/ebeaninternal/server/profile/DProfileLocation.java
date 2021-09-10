package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;

/**
 * Default profile location that uses stack trace.
 */
class DProfileLocation implements ProfileLocation {

  private static final String IO_EBEAN = "io.ebean";
  private static final String UNKNOWN = "unknown";

  private String fullLocation;
  private String location;
  private String label;
  private final int lineNumber;
  private int traceCount;

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
  public boolean obtain() {
    // atomic assignments so happy enough with this (racing but atomic)
    if (fullLocation != null) {
      return false;
    }
    final String loc = create();
    final String location = UtilLocation.loc(loc);
    this.label = UtilLocation.label(location);
    this.location = location;
    this.fullLocation = loc;
    initWith(label);
    return true;
  }

  protected void initWith(String label) {
    // nothing by default
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
    // racey but atomic and no problem with over or under tracing
    if (traceCount <= 0) {
      return false;
    } else {
      traceCount--;
      return true;
    }
  }

  @Override
  public void setTraceCount(int traceCount) {
    this.traceCount = traceCount;
  }

  private String create() {
    // relatively expensive but we only do it once per profile location
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
}

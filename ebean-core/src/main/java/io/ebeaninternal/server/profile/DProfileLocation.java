package io.ebeaninternal.server.profile;

import io.ebean.ProfileLocation;
import io.ebean.util.StackWalkFilter;

import java.util.stream.Stream;

/**
 * Default profile location that uses stack trace.
 */
class DProfileLocation implements ProfileLocation {

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
    return StackWalker.getInstance().walk(this::filter);
  }

  private String filter(Stream<StackWalker.StackFrame> frames) {
    return frames.filter(StackWalkFilter.filter())
      .findFirst()
      .map(line -> withLineNumber(line.toString()))
      .orElse(UNKNOWN);
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

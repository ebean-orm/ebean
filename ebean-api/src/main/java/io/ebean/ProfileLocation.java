package io.ebean;

/**
 * A location for profiling transactions and queries.
 * <p>
 * Typically represents a class method in the form of class file and line of code that started
 * the transaction or invoked the query.
 * </p>
 */
public interface ProfileLocation {

  /**
   * Create and return a new ProfileLocation.
   */
  static ProfileLocation create() {
    return XServiceProvider.profileLocationFactory().create();
  }

  /**
   * Create and return a new ProfileLocation with a given lineNumber and label.
   */
  static ProfileLocation create(int lineNumber, String label) {
    return XServiceProvider.profileLocationFactory().create(lineNumber, label);
  }

  /**
   * Create and return a new ProfileLocation with a given location.
   */
  static ProfileLocation createAt(String location) {
    return XServiceProvider.profileLocationFactory().createAt(location);
  }

  /**
   * Obtain the description returning true if this is the initial call.
   */
  boolean obtain();

  /**
   * Return a short version of the location description.
   */
  String location();

  /**
   * Return the short label.
   */
  String label();

  /**
   * Return the full location.
   */
  String fullLocation();

  /**
   * Add execution time.
   */
  void add(long executionTime);

  /**
   * Return true if this request should be traced.
   */
  boolean trace();

  /**
   * Set the number of times to trace the transactions for this profile location.
   */
  void setTraceCount(int traceCount);
}

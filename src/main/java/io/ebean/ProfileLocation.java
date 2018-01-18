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
   * Create and return a new ProfileLocation with a given lineNumber.
   */
  static ProfileLocation create(int lineNumber) {
    return XServiceProvider.profileLocationFactory().create(lineNumber);
  }

  /**
   * Create and return a new ProfileLocation with a given location.
   */
  static ProfileLocation create(String location) {
    return XServiceProvider.profileLocationFactory().create(location);
  }

  /**
   * Obtain the location description.
   */
  String obtain();
}

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
   * Obtain the location description.
   */
  String obtain();

  /**
   * Return a short version of the location description.
   */
  String shortDescription();

  /**
   * Add execution time.
   */
  void add(long executionTime);
}

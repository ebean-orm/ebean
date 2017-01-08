package io.ebean;

import io.ebean.config.CurrentUserProvider;

/**
 * Returns the current user typically from a Thread local or similar context.
 */
public class MyCurrentUserProvider implements CurrentUserProvider {

  public static final String DEFAULT = "42789";

  /**
   * Do not do this yourself - this is for testing purposes.
   */
  private static Object userId = DEFAULT;


  @Override
  public Object currentUser() {
    // just hardcoding here for testing
    return userId;
  }

  public static void setUser(Object value) {
    userId = value;
  }

  public static void resetToDefault() {
    userId = DEFAULT;
  }

}

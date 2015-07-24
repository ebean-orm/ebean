package com.avaje.ebean;

import com.avaje.ebean.config.CurrentUserProvider;

/**
 * Returns the current user typically from a Thread local or similar context.
 */
public class MyCurrentUserProvider implements CurrentUserProvider {

  public static final String DEFAULT = "42789";

  /**
   * Do not do this yourself - this is for testing purposes.
   */
  private static String userId = DEFAULT;


  @Override
  public Object currentUser() {
    // just hardcoding here for testing
    return userId;
  }

  public static void setUserId(String value) {
    userId = value;
  }

  public static void resetToDefault() {
    userId = DEFAULT;
  }

}

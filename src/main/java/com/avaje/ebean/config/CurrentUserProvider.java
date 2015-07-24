package com.avaje.ebean.config;

/**
 * Provides the current user in order to support 'Who created', 'Who modified' and other audit features.
 */
public interface CurrentUserProvider {

  /**
   * Return the current user id.
   * <p>
   *   The type returned should match the type of the properties annotated
   *   with @WhoCreated and @WhoModified. These are typically String, Long or UUID.
   * </p>
   */
  Object currentUser();
}

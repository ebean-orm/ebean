package io.ebean.config;

import java.lang.invoke.MethodHandles.Lookup;

/**
 * Provides a Lookup instance for accessing entity/dto fields.
 */
public interface LookupProvider {

  /**
   * Return the Lookup.
   */
  Lookup provideLookup();

}

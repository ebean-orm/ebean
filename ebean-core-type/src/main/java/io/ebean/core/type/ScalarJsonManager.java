package io.ebean.core.type;

import io.ebean.annotation.MutationDetection;

/**
 * Holds the configured mapper and default mutation detection mode to be used.
 */
public interface ScalarJsonManager {

  /**
   * Return the default mutation detection mode.
   */
  MutationDetection mutationDetection();

  /**
   * Return the object mapper supplied in DatabaseConfig.
   */
  Object mapper();

  /**
   * Return the Postgres type given the jdbc type.
   */
  String postgresType(int dbType);
}

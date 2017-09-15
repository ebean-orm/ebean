package io.ebean.config;

import io.ebean.config.dbplatform.DatabasePlatform;

/**
 * Can be specified in {@link DbMigrationConfig} to provide missing default values, count or fail missing values.
 * @author Roland Praml, FOCONIS AG
 *
 */
@FunctionalInterface
public interface DbMigrationDefaultValueProvider {

  String getDefaultValue(DatabasePlatform platform, String table, String column, String type);
}

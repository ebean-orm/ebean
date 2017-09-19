package io.ebean.config;

import io.ebean.annotation.DbDefault;
import io.ebean.config.dbplatform.DatabasePlatform;

/**
 * This callback is called, when db-migration needs a default value (for non null columns) 
 * and no default value is set with {@link DbDefault}.
 * You can implement your own strategy, how to get the default value
 * <ul>
 * <li>return '0', 0, false, ... depending on the type</li>
 * <li>throw an exception to stop migration generation</li>
 * <li>prompt user</li>
 * <li>...</li>
 * </ul>
 *  
 * @author Roland Praml, FOCONIS AG
 *
 */
@FunctionalInterface
public interface DbMigrationDefaultValueProvider {

  String getDefaultValue(DatabasePlatform platform, String table, String column, String type);
}

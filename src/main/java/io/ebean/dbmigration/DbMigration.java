package io.ebean.dbmigration;

import io.ebean.EbeanServer;
import io.ebean.annotation.Platform;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.DatabasePlatform;

import java.io.IOException;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Generates DDL migration scripts based on changes to the current model.
 *
 * <p>
 * Typically this is run as a main method in src/test once a developer is happy
 * with the next set of changes to the model.
 * </p>
 *
 * <h3>Example: Run for a single specific platform</h3>
 *
 * <pre>{@code
 *
 *    // optionally specify the version and name
 *    //System.setProperty("ddl.migration.version", "1.1");
 *    //System.setProperty("ddl.migration.name", "add bars");
 *
 *    // generate a migration using drops from a prior version
 *    //System.setProperty("ddl.migration.pendingDropsFor", "1.2");
 *
 *    DbMigration migration = DbMigration.create();
 *
 *    migration.setPlatform(Platform.POSTGRES);
 *    migration.generateMigration();
 *
 * }</pre>
 */
public interface DbMigration {

  /**
   * Create a DbMigration implementation to use.
   */
  static DbMigration create() {

    Iterator<DbMigration> loader = ServiceLoader.load(DbMigration.class).iterator();
    if (loader.hasNext()) {
      return loader.next();
    }
    throw new IllegalStateException("No service implementation found for DbMigration?");
  }

  /**
   * Set the path from the current working directory to the application resources.
   * <p>
   * This defaults to maven style 'src/main/resources'.
   */
  void setPathToResources(String pathToResources);

  /**
   * Set the server to use to determine the current model.
   * Typically this is not called explicitly.
   */
  void setServer(EbeanServer ebeanServer);

  /**
   * Set the serverConfig to use. Typically this is not called explicitly.
   */
  void setServerConfig(ServerConfig config);

  /**
   * Set the specific platform to generate DDL for.
   * <p>
   * If not set this defaults to the platform of the default server.
   * </p>
   */
  void setPlatform(Platform platform);

  /**
   * Set the specific platform to generate DDL for.
   * <p>
   * If not set this defaults to the platform of the default server.
   * </p>
   */
  void setPlatform(DatabasePlatform databasePlatform);

  /**
   * Set to false to turn off strict mode.
   * <p>
   * Strict mode checks that a column changed to non-null on an existing table via DB migration has a default
   * value specified. Set this to false if that isn't the case but it is known that all the existing rows have
   * a value specified (there are no existing null values for the column).
   * </p>
   */
  void setStrictMode(boolean strictMode);

  /**
   * Set to true to include a generated header comment in the DDL script.
   */
  void setIncludeGeneratedFileComment(boolean includeGeneratedFileComment);

  /**
   * Set this to false to exclude the builtin support for table partitioning (with @DbPartition).
   */
  void setIncludeBuiltInPartitioning(boolean includeBuiltInPartitioning);

  /**
   * Set the header that is included in the generated DDL script.
   */
  void setHeader(String header);

  /**
   * Set the prefix for the version. Set this to "V" for use with Flyway.
   */
  void setApplyPrefix(String applyPrefix);

  /**
   * Set the version of the migration to be generated.
   */
  void setVersion(String version);

  /**
   * Set the name of the migration to be generated.
   */
  void setName(String name);

  /**
   * Generate a migration for the version specified that contains pending drops.
   *
   * @param generatePendingDrop The version of a prior migration that holds pending drops.
   */
  void setGeneratePendingDrop(String generatePendingDrop);

  /**
   * Add an additional platform to write the migration DDL.
   * <p>
   * Use this when you want to generate sql scripts for multiple database platforms
   * from the migration (e.g. generate migration sql for MySql, Postgres and Oracle).
   * </p>
   */
  void addPlatform(Platform platform, String prefix);

  /**
   * Add an additional databasePlatform to write the migration DDL.
   * <p>
   * Use this when you want to add preconfigured database platforms.
   * </p>
   */
  void addDatabasePlatform(DatabasePlatform databasePlatform, String prefix);

  /**
   * Generate the next migration xml file and associated apply and rollback sql scripts.
   * <p>
   * This does not run the migration or ddl scripts but just generates them.
   * </p>
   * <h3>Example: Run for a single specific platform</h3>
   * <pre>{@code
   *
   *   DbMigration migration = DbMigration.create();
   *   migration.setPlatform(Platform.POSTGRES);
   *
   *   migration.generateMigration();
   *
   * }</pre>
   * <p>
   * <h3>Example: Run migration generating DDL for multiple platforms</h3>
   * <pre>{@code
   *
   *   DbMigration migration = DbMigration.create();
   *
   *   migration.setPathToResources("src/main/resources");
   *
   *   migration.addPlatform(Platform.POSTGRES, "pg");
   *   migration.addPlatform(Platform.MYSQL, "mysql");
   *   migration.addPlatform(Platform.ORACLE, "oracle");
   *
   *   migration.generateMigration();
   *
   * }</pre>
   *
   * @return the version of the generated migration or null
   */
  String generateMigration() throws IOException;
}

package com.avaje.ebean.dbmigration.runner;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.config.DbMigrationConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.dbmigration.MigrationRunner;
import com.avaje.ebean.plugin.SpiServer;
import com.avaje.ebeaninternal.server.transaction.ExternalJdbcTransaction;
import com.avaje.ebeaninternal.util.IOUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the migration table.
 */
public class MigrationTable {

  private static final Logger logger = MigrationRunner.logger;

  private final Connection connection;

  private final EbeanServer server;

  private final DatabasePlatform databasePlatform;

  private final String catalog;
  private final String schema;
  private final String table;
  private final ServerConfig serverConfig;
  private final String envUserName;

  private final Timestamp runTime = new Timestamp(System.currentTimeMillis());

  private final ScriptTransform scriptTransform;

  private final String insertSql;

  private final String updateSql;

  private final LinkedHashMap<String, MigrationMetaRow> migrations;

  private MigrationMetaRow lastMigration;

  /**
   * Construct with server, configuration and jdbc connection (DB admin user).
   */
  public MigrationTable(EbeanServer server, DbMigrationConfig migrationConfig, Connection connection) {
    this.connection = connection;
    this.server = server;
    this.migrations = new LinkedHashMap<String, MigrationMetaRow>();

    SpiServer pluginApi = server.getPluginApi();
    this.serverConfig = pluginApi.getServerConfig();
    this.databasePlatform = pluginApi.getDatabasePlatform();

    this.catalog = null;
    this.schema = null;
    this.table = migrationConfig.getMetaTable();
    this.insertSql = MigrationMetaRow.insertSql(table);
    this.updateSql = MigrationMetaRow.updateSql(table);

    this.scriptTransform = createScriptTransform(migrationConfig);
    this.envUserName = System.getProperty("user.name");
  }

  /**
   * Return the number of migrations in the DB migration table.
   */
  public int size() {
    return migrations.size();
  }

  /**
   * Create the ScriptTransform for placeholder key/value replacement.
   */
  private ScriptTransform createScriptTransform(DbMigrationConfig config) {

    Map<String, String> map = PlaceholderBuilder.build(config.getRunPlaceholders(), config.getRunPlaceholderMap());
    return new ScriptTransform(map);
  }

  /**
   * Create the table is it does not exist.
   */
  public void createIfNeeded() throws SQLException, IOException {

    if (!tableExists(connection)) {
      createTable(connection);
    }

    ExternalJdbcTransaction t = new ExternalJdbcTransaction(connection);
    SqlQuery sqlQuery = server.createSqlQuery("select * from " + table + " order by id for update");
    List<SqlRow> metaRows = server.findList(sqlQuery, t);

    for (SqlRow row : metaRows) {
      MigrationMetaRow metaRow = new MigrationMetaRow(row);
      addMigration(metaRow.getVersion(), metaRow);
    }
  }


  private void createTable(Connection connection) throws IOException, SQLException {

    String script = ScriptTransform.table(table, getCreateTableScript());

    MigrationScriptRunner run = new MigrationScriptRunner(connection);
    run.runScript(false, script, "create migration table");
  }

  /**
   * Return the create table script.
   */
  private String getCreateTableScript() throws IOException {
    // supply a script to override the default table create script
    String script = readResource("migration-support/create-table.sql");
    if (script == null) {
      // no, just use the default script
      script = readResource("migration-support/default-create-table.sql");
    }
    return script;
  }

  private String readResource(String location) throws IOException {

    Enumeration<URL> resources = serverConfig.getClassLoadConfig().getResources(location);
    if (resources.hasMoreElements()) {
      URL url = resources.nextElement();
      return IOUtils.readUtf8(url.openStream());
    }
    return null;
  }

  /**
   * Return true if the table exists.
   */
  private boolean tableExists(Connection connection) throws SQLException {
    return databasePlatform.tableExists(connection, catalog, schema, table);
  }

  /**
   * Return true if the migration ran successfully and false if the migration failed.
   */
  public boolean shouldRun(LocalMigrationResource localVersion, LocalMigrationResource priorVersion) throws SQLException {

    if (priorVersion != null && !localVersion.isRepeatable()) {
      if (!migrationExists(priorVersion)) {
        logger.error("Migration {} requires prior migration {} which has not been run", localVersion.getVersion(), priorVersion.getVersion());
        return false;
      }
    }

    MigrationMetaRow existing = migrations.get(localVersion.key());
    return runMigration(localVersion, existing);
  }

  /**
   * Run the migration script.
   *
   * @param local    The local migration resource
   * @param existing The information for this migration existing in the table
   *
   * @return True if the migrations should continue
   */
  private boolean runMigration(LocalMigrationResource local, MigrationMetaRow existing) throws SQLException {

    String script = convertScript(local.getContent());
    int checksum = Checksum.calculate(script);

    if (existing != null) {

      boolean matchChecksum = (existing.getChecksum() == checksum);

      if (!local.isRepeatable()) {
        if (!matchChecksum) {
          logger.error("Checksum mismatch on migration {}", local.getLocation());
        }
        return true;

      } else if (matchChecksum) {
        logger.trace("... skip unchanged repeatable migration {}", local.getLocation());
        return true;
      }
    }

    runMigration(local, existing, script, checksum);
    return true;
  }

  /**
   * Run a migration script as new migration or update on existing repeatable migration.
   */
  private void runMigration(LocalMigrationResource local, MigrationMetaRow existing, String script, int checksum) throws SQLException {

    logger.debug("run migration {}", local.getLocation());

    MigrationScriptRunner run = new MigrationScriptRunner(connection);
    run.runScript(false, script, "run migration version: " + local.getVersion());

    if (existing != null) {
      // update existing migration row
      SqlUpdate update = server.createSqlUpdate(updateSql);
      existing.bindUpdate(checksum, envUserName, runTime, update);
      server.execute(update, new ExternalJdbcTransaction(connection));

    } else {
      // insert new migration row
      SqlUpdate insert = server.createSqlUpdate(insertSql);
      MigrationMetaRow metaRow = createMetaRow(local, checksum);
      metaRow.bindInsert(insert);
      server.execute(insert, new ExternalJdbcTransaction(connection));

      addMigration(local.key(), metaRow);
    }
  }

  /**
   * Create the MigrationMetaRow for this migration.
   */
  private MigrationMetaRow createMetaRow(LocalMigrationResource migration, int checksum) {

    int nextId = 1;
    if (lastMigration != null) {
      nextId = lastMigration.getId() + 1;
    }

    String type = migration.getType();
    String runVersion = migration.key();
    String comment = migration.getComment();

    return new MigrationMetaRow(nextId, type, runVersion, comment, checksum, envUserName, runTime);
  }

  /**
   * Return true if the migration exists.
   */
  private boolean migrationExists(LocalMigrationResource priorVersion) {
    return migrations.containsKey(priorVersion.key());
  }

  /**
   * Apply the placeholder key/value replacement on the script.
   */
  private String convertScript(String script) {
    return scriptTransform.transform(script);
  }

  /**
   * Register the successfully executed migration (to allow dependant scripts to run).
   */
  private void addMigration(String key, MigrationMetaRow metaRow) {
    lastMigration = metaRow;
    if (metaRow.getVersion() == null) {
      throw new IllegalStateException("No runVersion in db migration table row? " + metaRow);
    }
    migrations.put(key, metaRow);
  }
}

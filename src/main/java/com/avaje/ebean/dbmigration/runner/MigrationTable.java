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
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the migration table.
 */
public class MigrationTable {

  private static final Logger logger = MigrationRunner.logger;

  private final  Connection connection;

  private final EbeanServer server;

  private final DatabasePlatform databasePlatform;

  private final String catalog;
  private final String schema;
  private final String table;
  private final ServerConfig serverConfig;
  private final String envUserName;

  private final ScriptTransform scriptTransform;

  private final String insertSql;

  private final LinkedHashMap<String,MigrationMetaRow> migrations;

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
    SqlQuery sqlQuery = server.createSqlQuery("select * from "+table+" order by id for update");
    List<SqlRow> metaRows = server.findList(sqlQuery, t);

    for (SqlRow row : metaRows) {
      addMigration(new MigrationMetaRow(row));
    }
  }


  private void createTable(Connection connection) throws IOException {

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
  public boolean shouldRun(LocalMigrationResource localVersion, LocalMigrationResource priorVersion) {

    if (priorVersion != null) {
      // check priorVersion is installed
      MigrationMetaRow existing = migrations.get(priorVersion.getVersion().normalised());
      if (existing == null) {
        logger.warn("Migration {} requires prior migration {} which has not been run", localVersion.getVersion(), priorVersion.getVersion());
        return false;
      }
    }

    MigrationMetaRow existing = migrations.get(localVersion.getVersion().normalised());
    if (existing == null) {
      runMigration(localVersion, priorVersion);
      return true;

    } else {
      // check checksum and return ok, or re-run if repeatable script?
      existing.getChecksum();
      return true;
    }
  }

  /**
   * Run the migration script.
   */
  private void runMigration(LocalMigrationResource localVersion, LocalMigrationResource prior) {

    logger.debug("run migration "+localVersion.getLocation());

    String script = convertScript(localVersion.getContent());

    MigrationScriptRunner run = new MigrationScriptRunner(connection);
    run.runScript(false, script, "run migration version: "+localVersion.getVersion());


    int checksum = Checksum.calculate(script);
    MigrationMetaRow metaRow = createMetaRow(localVersion, prior, checksum);

    SqlUpdate insert = server.createSqlUpdate(insertSql);
    metaRow.bindInsert(insert);
    server.execute(insert, new ExternalJdbcTransaction(connection));

    addMigration(metaRow);
  }

  /**
   * Create the MigrationMetaRow for this migration.
   */
  private MigrationMetaRow createMetaRow(LocalMigrationResource localVersion, LocalMigrationResource prior, int checksum) {

    int nextId = 1;
    if (lastMigration != null) {
      nextId = lastMigration.getId() + 1;
    }

    String runVersion = localVersion.getVersion().normalised();
    String comment = getMigrationComment(localVersion);
    String priorVersion = getMigrationPriorVersion(prior);

    return new MigrationMetaRow(nextId, runVersion, priorVersion, comment, checksum, envUserName);
  }

  /**
   * Return the prior migration normalised version.
   */
  private String getMigrationPriorVersion(LocalMigrationResource prior) {
    return prior != null ? prior.getVersion().normalised() : "-";
  }

  /**
   * Return the migration comment.
   */
  private String getMigrationComment(LocalMigrationResource localVersion) {
    String comment = localVersion.getVersion().getComment();
    return comment == null || comment.isEmpty() ? "-" : comment;
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
  private void addMigration(MigrationMetaRow metaRow) {
    lastMigration = metaRow;
    String runVersion = metaRow.getRunVersion();
    if (runVersion == null) {
      throw new IllegalStateException("No runVersion in db migration table row? " + metaRow);
    }
    migrations.put(runVersion, metaRow);
  }
}

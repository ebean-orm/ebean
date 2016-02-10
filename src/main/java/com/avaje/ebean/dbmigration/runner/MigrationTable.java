package com.avaje.ebean.dbmigration.runner;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.config.DbMigrationConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.plugin.SpiServer;
import com.avaje.ebeaninternal.server.transaction.ExternalJdbcTransaction;
import com.avaje.ebeaninternal.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by rob on 5/02/16.
 */
public class MigrationTable {

  private static final Logger logger = LoggerFactory.getLogger(MigrationTable.class);

  private final  Connection connection;

  private final EbeanServer server;

  private final DatabasePlatform databasePlatform;

  private final DbMigrationConfig migrationConfig;

  private final String catalog;
  private final String schema;
  private final String table;
  private final ServerConfig serverConfig;
  private final String envUserName;

  int currentSearchPosition;

  List<SqlRow> metaRows;

  public MigrationTable(EbeanServer server, DbMigrationConfig migrationConfig, Connection connection) {
    this.connection = connection;
    this.server = server;

    SpiServer pluginApi = server.getPluginApi();
    this.serverConfig = pluginApi.getServerConfig();
    this.databasePlatform = pluginApi.getDatabasePlatform();
    this.migrationConfig = migrationConfig;
    this.catalog = null;
    this.schema = null;
    this.table = "ebean_migration";

    this.envUserName = System.getProperty("user.name");
  }


  public void createIfNeeded() throws SQLException, IOException {

    if (!tableExists(connection)) {
      createTable(connection);
    }

    ExternalJdbcTransaction t = new ExternalJdbcTransaction(connection);
    SqlQuery sqlQuery = server.createSqlQuery("select * from ebean_migration order by id for update");
    metaRows = server.findList(sqlQuery, t);
  }

  private void createTable(Connection connection) throws IOException {

    String script = getCreateTableScript();
    MigrationScriptRunner run = new MigrationScriptRunner(connection);
    run.runScript(false, script, "create migration tables");
  }

  private String getCreateTableScript() throws IOException {

    Enumeration<URL> resources = serverConfig.getClassLoadConfig().getResources("migration-support/create.sql");
    if (resources.hasMoreElements()) {
      URL url = resources.nextElement();
      return IOUtils.readUtf8(url.openStream());
    }
    return null;
  }

  public boolean tableExists(Connection connection) throws SQLException {

    return databasePlatform.tableExists(connection, catalog, schema, table);
  }


  public boolean shouldRun(LocalMigrationResource localVersion, LocalMigrationResource priorVersion) {

    // if prior != null check previous version installed
    //    if previous version not installed ... error - missing version (prior version)

//    if (!priorVersionNotInstalled(priorVersion)) {
//
//    }
//
//    // if localVersion installed
//    //    check checksum and return ok, or re-installable?
//    // else
//    //    install version and continue
//
//    if (runPosition >= metaRows.size()) {
//      logger.debug("No matching row");
//      runMigration(runPosition, localVersion);
//      return true;
//    }

    return false;
  }

  private void checkInstalled(LocalMigrationResource priorVersion) {
    if (priorVersion != null) {
      searchFor(priorVersion);
    }
  }

  private void searchFor(LocalMigrationResource priorVersion) {

  }

  public void commit() throws SQLException {
    connection.commit();
  }

  private void runMigration(int runPosition, LocalMigrationResource localVersion) {

    logger.debug("run migration "+localVersion.getLocation());


    String script = localVersion.getContent();

    MigrationScriptRunner run = new MigrationScriptRunner(connection);
    run.runScript(false, script, "run migration version: "+localVersion.getVersion());

    String sql = "insert into ebean_migration (id,status,run_version,dep_version,comment,checksum,run_on,run_by,run_ip) "+
        "values (?,?,?,?,?,?,?,?,?)";

    SqlUpdate sqlUpdate = server.createSqlUpdate(sql);
    sqlUpdate.setParameter(1, runPosition+1);
    sqlUpdate.setParameter(2, "success");
    sqlUpdate.setParameter(3, localVersion.getVersion().getFull());
    sqlUpdate.setParameter(4, "na");
    sqlUpdate.setParameter(5, "comm");
    sqlUpdate.setParameter(6, 0);
    sqlUpdate.setParameter(7, new Timestamp(System.currentTimeMillis()));
    sqlUpdate.setParameter(8, envUserName);
    sqlUpdate.setParameter(9, "userIp");

    ExternalJdbcTransaction t = new ExternalJdbcTransaction(connection);
    server.execute(sqlUpdate, t);

  }
}

package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.DatabaseBuilder;
import io.ebean.config.DbConstraintNaming;

/**
 * Base implementation for all histories, where we must maintain history table (trigger based, db2 and hana)
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public abstract class DbTableBasedHistoryDdl implements PlatformHistoryDdl.TableBased {

  private DbConstraintNaming constraintNaming;
  private String historySuffix;
  protected PlatformDdl platformDdl;

  @Override
  public void configure(DatabaseBuilder config, PlatformDdl platformDdl) {
    this.platformDdl = platformDdl;
    this.historySuffix = config.getHistoryTableSuffix();
    this.constraintNaming = config.getConstraintNaming();
  }

  @Override
  public String historyTableName(String baseTableName) {
    return normalise(baseTableName, historySuffix);
  }

  protected String normalise(String tableName, String suffix) {
    String normalized = quote(normalise(tableName) + suffix);
    int lastPeriod = tableName.lastIndexOf('.');
    return tableName.substring(0, lastPeriod + 1) + normalized;
  }

  protected String normalise(String tableName) {
    return constraintNaming.normaliseTable(tableName);
  }

  protected String quote(String dbName) {
    return platformDdl.quote(dbName);
  }

}

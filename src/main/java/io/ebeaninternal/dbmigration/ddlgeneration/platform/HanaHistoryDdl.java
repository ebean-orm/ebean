package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.ServerConfig;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.model.MColumn;
import io.ebeaninternal.dbmigration.model.MTable;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HanaHistoryDdl implements PlatformHistoryDdl {

  private String systemPeriodStart;
  private String systemPeriodEnd;
  private PlatformDdl platformDdl;
  private String historySuffix;
  private final AtomicInteger counter = new AtomicInteger(0);
  private Map<String, String> createdHistoryTables = new ConcurrentHashMap<>();

  @Override
  public void configure(ServerConfig serverConfig, PlatformDdl platformDdl) {
    this.systemPeriodStart = serverConfig.getAsOfSysPeriod() + "_start";
    this.systemPeriodEnd = serverConfig.getAsOfSysPeriod() + "_end";
    this.platformDdl = platformDdl;
    this.historySuffix = serverConfig.getHistoryTableSuffix();
  }

  @Override
  public void createWithHistory(DdlWrite writer, MTable table) throws IOException {
    String tableName = table.getName();
    String historyTableName = tableName + historySuffix;
    DdlBuffer apply = writer.applyHistoryView();
    if (apply.isEmpty()) {
      createdHistoryTables.clear();
    }

    apply.append(platformDdl.getCreateTableCommandPrefix()).append(" ").append(historyTableName).append(" (").newLine();

    // create history table
    Collection<MColumn> cols = table.allColumns();
    for (MColumn column : cols) {
      if (!column.isDraftOnly()) {
        writeColumnDefinition(apply, column.getName(), column.getType(), column.getDefaultValue(), column.isNotnull(),
          column.isIdentity() ? platformDdl.identitySuffix : null);
        apply.append(",").newLine();
      }
    }
    writeColumnDefinition(apply, systemPeriodStart, "TIMESTAMP", null, false, null);
    apply.append(",").newLine();
    writeColumnDefinition(apply, systemPeriodEnd, "TIMESTAMP", null, false, null);
    apply.newLine().append(")").endOfStatement();

    // enable system versioning
    apply.append("alter table ").append(tableName).append(" add (").newLine();
    apply.append("    ").append(systemPeriodStart).append(" TIMESTAMP NOT NULL GENERATED ALWAYS AS ROW START, ").newLine();
    apply.append("    ").append(systemPeriodEnd).append(" TIMESTAMP NOT NULL GENERATED ALWAYS AS ROW END").newLine();
    apply.append(")").endOfStatement();

    apply.append("alter table ").append(tableName).append(" add period for system_time(").append(systemPeriodStart)
      .append(",").append(systemPeriodEnd).append(")").endOfStatement();

    enableSystemVersioning(apply, tableName, historyTableName, true, false);

    createdHistoryTables.put(tableName, historyTableName);

    dropHistoryTable(writer.dropAll(), tableName, historyTableName);
  }

  @Override
  public void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) throws IOException {
    dropHistoryTable(writer.applyDropDependencies(), dropHistoryTable.getBaseTable(),
      dropHistoryTable.getBaseTable() + historySuffix);
  }

  protected void dropHistoryTable(DdlBuffer apply, String baseTable, String historyTable) throws IOException {
    // disable system versioning
    disableSystemVersioning(apply, baseTable);

    apply.append("alter table ").append(baseTable).append(" drop period for system_time").endOfStatement();

    // drop the period columns
    apply.append("alter table ").append(baseTable).append(" drop (").append(systemPeriodStart).append(",")
      .append(systemPeriodEnd).append(")").endOfStatement();

    // drop the history table
    apply.append("drop table ").append(historyTable).append(" cascade").endOfStatement();
  }

  @Override
  public void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) throws IOException {
    MTable table = writer.getTable(addHistoryTable.getBaseTable());
    if (table == null) {
      throw new IllegalStateException("MTable " + addHistoryTable.getBaseTable() + " not found in writer? (required for history DDL)");
    }
    createWithHistory(writer, table);
  }

  @Override
  public void updateTriggers(DdlWrite write, HistoryTableUpdate baseTable) {
    // nothing to do
  }

  protected void writeColumnDefinition(DdlBuffer buffer, String columnName, String type, String defaultValue,
                                       boolean isNotNull, String generated) throws IOException {

    String platformType = platformDdl.convert(type, false);
    buffer.append(" ").append(platformDdl.lowerColumnName(columnName));
    buffer.append(" ").append(platformType);
    if (defaultValue != null) {
      buffer.append(" default ").append(defaultValue);
    }
    if (isNotNull) {
      buffer.append(" not null");
    }
    if (generated != null) {
      buffer.append(" ").append(generated);
    }
  }

  public void disableSystemVersioning(DdlBuffer apply, String tableName) throws IOException {
    disableSystemVersioning(apply, tableName, false);
  }

  public void disableSystemVersioning(DdlBuffer apply, String tableName, boolean uniqueStatement) throws IOException {
    apply.append("alter table ").append(tableName).append(" drop system versioning");
    if (uniqueStatement) {
      // needed for the DB migration test to prevent the statement from being filtered
      // out as a duplicate
      apply.append(" /* ").append(String.valueOf(counter.getAndIncrement())).append(" */");
    }
    apply.endOfStatement();
  }

  public void enableSystemVersioning(DdlBuffer apply, String tableName, String historyTableName, boolean validated,
                                     boolean uniqueStatement) throws IOException {
    apply.append("alter table ").append(tableName).append(" add system versioning history table ").append(historyTableName);
    if (!validated) {
      apply.append(" not validated");
    }
    if (uniqueStatement) {
      // needed for the DB migration test to prevent the statement from being filtered
      // out as a duplicate
      apply.append(" /* ").append(String.valueOf(counter.getAndIncrement())).append(" */");
    }
    apply.endOfStatement();
  }

  public boolean isSystemVersioningEnabled(String tableName) {
    return !createdHistoryTables.containsKey(tableName);
  }
}

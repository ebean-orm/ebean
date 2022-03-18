package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlAlterTable;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.model.MColumn;
import io.ebeaninternal.dbmigration.model.MTable;

import java.util.Collection;

public class HanaHistoryDdl implements PlatformHistoryDdl {

  private String systemPeriodStart;
  private String systemPeriodEnd;
  private PlatformDdl platformDdl;
  private String historySuffix;

  @Override
  public void configure(DatabaseConfig config, PlatformDdl platformDdl) {
    this.systemPeriodStart = config.getAsOfSysPeriod() + "_start";
    this.systemPeriodEnd = config.getAsOfSysPeriod() + "_end";
    this.platformDdl = platformDdl;
    this.historySuffix = config.getHistoryTableSuffix();
  }

  @Override
  public void createWithHistory(DdlWrite writer, MTable table) {
    String tableName = table.getName();
    String historyTableName = tableName + historySuffix;
    DdlBuffer apply = writer.applyPostAlter();

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

    enableSystemVersioning(apply, tableName, true);
    platformDdl.alterTable(writer, tableName).setHistoryHandled();

    dropHistoryTable(writer.dropAll(), tableName, historyTableName);
  }

  @Override
  public void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) {
    dropHistoryTable(writer.applyDropDependencies(), dropHistoryTable.getBaseTable(),
      dropHistoryTable.getBaseTable() + historySuffix);
  }

  protected void dropHistoryTable(DdlBuffer apply, String baseTable, String historyTable) {
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
  public void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) {
    MTable table = writer.getTable(addHistoryTable.getBaseTable());
    if (table == null) {
      throw new IllegalStateException("MTable " + addHistoryTable.getBaseTable() + " not found in writer? (required for history DDL)");
    }
    createWithHistory(writer, table);
  }

  @Override
  public boolean alterHistoryTables() {
    return true;
  }

  @Override
  public void updateTriggers(DdlWrite writer, String tableName) {
    DdlAlterTable alter = platformDdl.alterTable(writer, tableName);
    MTable table = writer.getTable(tableName);
    if (table.isWithHistory() && !alter.isHistoryHandled()) {
      disableSystemVersioning(writer.apply(), tableName);
      enableSystemVersioning(writer.applyPostAlter(), tableName, false);
      alter.setHistoryHandled();
    }
  }

  protected void writeColumnDefinition(DdlBuffer buffer, String columnName, String type, String defaultValue,
                                       boolean isNotNull, String generated) {

    String platformType = platformDdl.convert(type);
    buffer.append(" ").append(columnName);
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

  public void disableSystemVersioning(DdlBuffer apply, String tableName) {
    apply.append("alter table ").append(tableName).append(" drop system versioning").endOfStatement();
  }

  public void enableSystemVersioning(DdlBuffer apply, String tableName, boolean validated) {
    apply.append("alter table ").append(tableName).append(" add system versioning history table ").append(tableName).append(historySuffix);
    if (!validated) {
      apply.append(" not validated");
    }
    apply.endOfStatement();
  }

}

package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.DatabaseConfig;
import io.ebean.config.PropertiesWrapper;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlAlterTable;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.AddColumn;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.Column;
import io.ebeaninternal.dbmigration.migration.DropColumn;
import io.ebeaninternal.dbmigration.model.MTable;

import java.util.List;

public class HanaTableDdl extends BaseTableDdl {

  private final HanaHistoryDdl historyDdl;

  public HanaTableDdl(DatabaseConfig config, PlatformDdl platformDdl) {
    super(config, platformDdl);
    this.historyDdl = (HanaHistoryDdl) platformDdl.historyDdl;
  }

  @Override
  public void generate(DdlWrite writer, AddColumn addColumn) {
    String tableName = addColumn.getTableName();
    MTable table = writer.getTable(tableName);
    if (table == null) {
      super.generate(writer, addColumn);
      return;
    }

    boolean manageSystemVersioning = isTrue(table.isWithHistory()) && historyDdl.isSystemVersioningEnabled(tableName);

    if (manageSystemVersioning) {
      DdlAlterTable alter = platformDdl.alterTable(writer, tableName);
      if (!alter.isHistoryHandled()) {
        alter.setHistoryHandled();
        historyDdl.disableSystemVersioning(writer.apply(), table.getName());
        historyDdl.enableSystemVersioning(writer.applyPostAlter(), table.getName(), false);
      }
    }

    super.generate(writer, addColumn);

    if (manageSystemVersioning) {
      // make same changes to the history table
      String historyTable = historyTable(tableName);
      List<Column> columns = addColumn.getColumn();
      for (Column column : columns) {
        alterTableAddColumn(writer, historyTable, column, true, true);
      }
    }
  }

  @Override
  public void generate(DdlWrite writer, AlterColumn alterColumn) {
    String tableName = alterColumn.getTableName();
    MTable table = writer.getTable(tableName);
    if (table == null) {
      super.generate(writer, alterColumn);
      return;
    }

    boolean manageSystemVersioning = isTrue(table.isWithHistory()) && historyDdl.isSystemVersioningEnabled(tableName);

    if (manageSystemVersioning) {
      DdlAlterTable alter = platformDdl.alterTable(writer, tableName);
      if (!alter.isHistoryHandled()) {
        alter.setHistoryHandled();
        historyDdl.disableSystemVersioning(writer.apply(), table.getName());
        historyDdl.enableSystemVersioning(writer.applyPostAlter(), table.getName(), false);
      }
    }

    super.generate(writer, alterColumn);

    if (manageSystemVersioning) {
      // make same changes to the history table
      String historyTable = historyTable(tableName);
      if (hasValue(alterColumn.getType()) || hasValue(alterColumn.getDefaultValue()) || alterColumn.isNotnull() != null) {
        AlterColumn alterHistoryColumn = new AlterColumn();
        alterHistoryColumn.setTableName(historyTable);
        alterHistoryColumn.setColumnName(alterColumn.getColumnName());
        alterHistoryColumn.setType(alterColumn.getType());
        alterHistoryColumn.setDefaultValue(alterColumn.getDefaultValue());
        alterHistoryColumn.setNotnull(alterColumn.isNotnull());
        alterHistoryColumn.setCurrentType(alterColumn.getCurrentType());
        alterHistoryColumn.setCurrentDefaultValue(alterColumn.getCurrentDefaultValue());
        alterHistoryColumn.setCurrentNotnull(alterColumn.isCurrentNotnull());
        platformDdl.alterColumn(writer, alterHistoryColumn);

      }
    }
  }

  @Override
  public void generate(DdlWrite writer, DropColumn dropColumn) {
    String tableName = dropColumn.getTableName();
    MTable table = writer.getTable(tableName);
    if (table == null) {
      super.generate(writer, dropColumn);
      return;
    }

    boolean manageSystemVersioning = isTrue(table.isWithHistory()) && historyDdl.isSystemVersioningEnabled(tableName);

    if (manageSystemVersioning) {
      DdlAlterTable alter = platformDdl.alterTable(writer, tableName);
      if (!alter.isHistoryHandled()) {
        alter.setHistoryHandled();
        historyDdl.disableSystemVersioning(writer.apply(), table.getName());
        historyDdl.enableSystemVersioning(writer.applyPostAlter(), tableName, false);
      }
    }

    super.generate(writer, dropColumn);

    if (manageSystemVersioning) {
      // also drop from the history table
      String historyTable = historyTable(tableName);
      alterTableDropColumn(writer, historyTable, dropColumn.getColumnName());

    }
  }

}

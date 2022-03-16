package io.ebeaninternal.dbmigration.ddlgeneration;

import io.ebeaninternal.dbmigration.migration.AddColumn;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.DropColumn;

/**
 * Write DDL for AddColumn , DropColumn or AlterColumn.
 */
public interface ColumnDdl {

  /**
   * Write the add column change.
   */
  void generate(DdlWrite writer, AddColumn addColumn);

  /**
   * Write the drop column change.
   */
  void generate(DdlWrite writer, DropColumn dropColumn);

  /**
   * Write the alter column changes.
   */
  void generate(DdlWrite writer, AlterColumn alterColumn);
}

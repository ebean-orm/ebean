package io.ebean.dbmigration.ddlgeneration;

import io.ebean.dbmigration.migration.AddColumn;
import io.ebean.dbmigration.migration.AlterColumn;
import io.ebean.dbmigration.migration.DropColumn;

import java.io.IOException;

/**
 * Write DDL for AddColumn , DropColumn or AlterColumn.
 */
public interface ColumnDdl {

  /**
   * Write the add column change.
   */
  void generate(DdlWrite writer, AddColumn addColumn) throws IOException;

  /**
   * Write the drop column change.
   */
  void generate(DdlWrite writer, DropColumn dropColumn) throws IOException;

  /**
   * Write the alter column changes.
   */
  void generate(DdlWrite writer, AlterColumn alterColumn) throws IOException;
}

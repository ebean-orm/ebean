package com.avaje.ebean.dbmigration.ddlgeneration;

import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.DropColumn;

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

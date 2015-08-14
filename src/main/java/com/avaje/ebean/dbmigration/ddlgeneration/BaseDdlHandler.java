package com.avaje.ebean.dbmigration.ddlgeneration;

import com.avaje.ebean.config.DbConstraintNaming;
import com.avaje.ebean.config.NamingConvention;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.BaseTableDdl;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.PlatformDdl;
import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.CreateTable;
import com.avaje.ebean.dbmigration.migration.DropColumn;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public class BaseDdlHandler implements DdlHandler {

  protected final TableDdl tableDdl;

  public BaseDdlHandler(NamingConvention namingConvention, DbConstraintNaming naming, PlatformDdl platformDdl) {
    this.tableDdl = new BaseTableDdl(namingConvention, naming, platformDdl);
  }

  @Override
  public void generate(DdlWrite writer, ChangeSet changeSet) throws IOException {

    List<Object> changeSetChildren = changeSet.getChangeSetChildren();
    for (Object change : changeSetChildren) {
      if (change instanceof  CreateTable) {
        generate(writer, (CreateTable) change);
      } else if (change instanceof AddColumn) {
        generate(writer, (AddColumn) change);
      } else if (change instanceof DropColumn) {
        generate(writer, (DropColumn) change);
      } else if (change instanceof AlterColumn) {
        generate(writer, (AlterColumn) change);
      }
    }
  }

  @Override
  public void generate(DdlWrite writer, CreateTable createTable) throws IOException {
    tableDdl.generate(writer, createTable);
  }

  @Override
  public void generate(DdlWrite writer, AddColumn addColumn) throws IOException {
    tableDdl.generate(writer, addColumn);
  }

  @Override
  public void generate(DdlWrite writer, DropColumn dropColumn) throws IOException {
    tableDdl.generate(writer, dropColumn);
  }

  @Override
  public void generate(DdlWrite writer, AlterColumn alterColumn) throws IOException {
    tableDdl.generate(writer, alterColumn);
  }
}

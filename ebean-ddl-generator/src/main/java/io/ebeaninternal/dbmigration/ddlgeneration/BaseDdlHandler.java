package io.ebeaninternal.dbmigration.ddlgeneration;

import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.BaseTableDdl;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import io.ebeaninternal.dbmigration.migration.AddColumn;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.AddTableComment;
import io.ebeaninternal.dbmigration.migration.AddUniqueConstraint;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.AlterForeignKey;
import io.ebeaninternal.dbmigration.migration.AlterTable;
import io.ebeaninternal.dbmigration.migration.ChangeSet;
import io.ebeaninternal.dbmigration.migration.CreateIndex;
import io.ebeaninternal.dbmigration.migration.CreateTable;
import io.ebeaninternal.dbmigration.migration.DropColumn;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropIndex;
import io.ebeaninternal.dbmigration.migration.DropTable;

import java.util.List;

/**
 *
 */
public class BaseDdlHandler implements DdlHandler {

  protected final TableDdl tableDdl;

  public BaseDdlHandler(DatabaseConfig config, PlatformDdl platformDdl) {
    this(config, platformDdl, new BaseTableDdl(config, platformDdl));
  }

  protected BaseDdlHandler(DatabaseConfig config, PlatformDdl platformDdl, TableDdl tableDdl) {
    this.tableDdl = tableDdl;
  }

  @Override
  public void generate(DdlWrite writer, ChangeSet changeSet) {

    List<Object> changeSetChildren = changeSet.getChangeSetChildren();
    for (Object change : changeSetChildren) {
      if (change instanceof CreateTable) {
        generate(writer, (CreateTable) change);
      }
    }
    for (Object change : changeSetChildren) {
      if (change instanceof CreateTable) {
        // ignore
      } else if (change instanceof DropTable) {
        generate(writer, (DropTable) change);
      } else if (change instanceof AlterTable) {
        generate(writer, (AlterTable) change);
      } else if (change instanceof AddTableComment) {
        generate(writer, (AddTableComment) change);
      } else if (change instanceof CreateIndex) {
        generate(writer, (CreateIndex) change);
      } else if (change instanceof DropIndex) {
        generate(writer, (DropIndex) change);
      } else if (change instanceof AddColumn) {
        generate(writer, (AddColumn) change);
      } else if (change instanceof DropColumn) {
        generate(writer, (DropColumn) change);
      } else if (change instanceof AlterColumn) {
        generate(writer, (AlterColumn) change);
      } else if (change instanceof AddHistoryTable) {
        generate(writer, (AddHistoryTable) change);
      } else if (change instanceof DropHistoryTable) {
        generate(writer, (DropHistoryTable) change);
      } else if (change instanceof AddUniqueConstraint) {
        generate(writer, (AddUniqueConstraint) change);
      } else if (change instanceof AlterForeignKey) {
        generate(writer, (AlterForeignKey) change);
      } else {
        throw new IllegalArgumentException("Unsupported change: " + change);
      }
    }
  }

  @Override
  public void generateProlog(DdlWrite writer) {
    tableDdl.generateProlog(writer);
  }

  @Override
  public void generateEpilog(DdlWrite writer) {
    tableDdl.generateEpilog(writer);
  }

  @Override
  public void generate(DdlWrite writer, CreateTable createTable) {
    tableDdl.generate(writer, createTable);
  }

  @Override
  public void generate(DdlWrite writer, DropTable dropTable) {
    tableDdl.generate(writer, dropTable);
  }

  @Override
  public void generate(DdlWrite writer, AlterTable alterTable) {
    tableDdl.generate(writer, alterTable);
  }

  @Override
  public void generate(DdlWrite writer, AddTableComment addTableComment) {
    tableDdl.generate(writer, addTableComment);
  }

  @Override
  public void generate(DdlWrite writer, AddColumn addColumn) {
    tableDdl.generate(writer, addColumn);
  }

  @Override
  public void generate(DdlWrite writer, DropColumn dropColumn) {
    tableDdl.generate(writer, dropColumn);
  }

  @Override
  public void generate(DdlWrite writer, AlterColumn alterColumn) {
    tableDdl.generate(writer, alterColumn);
  }

  @Override
  public void generate(DdlWrite writer, AddHistoryTable addHistoryTable) {
    tableDdl.generate(writer, addHistoryTable);
  }

  @Override
  public void generate(DdlWrite writer, DropHistoryTable dropHistoryTable) {
    tableDdl.generate(writer, dropHistoryTable);
  }

  @Override
  public void generate(DdlWrite writer, CreateIndex createIndex) {
    tableDdl.generate(writer, createIndex);
  }

  @Override
  public void generate(DdlWrite writer, DropIndex dropIndex) {
    tableDdl.generate(writer, dropIndex);
  }

  @Override
  public void generate(DdlWrite writer, AddUniqueConstraint constraint) {
    tableDdl.generate(writer, constraint);
  }

  @Override
  public void generate(DdlWrite writer, AlterForeignKey alterForeignKey) {
    tableDdl.generate(writer, alterForeignKey);
  }
}

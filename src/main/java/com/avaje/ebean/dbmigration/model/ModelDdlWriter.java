package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.Migration;
import com.avaje.ebean.dbmigration.migrationreader.MigrationXmlWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 */
public class ModelDdlWriter {


  private final ServerConfig serverConfig;

  private final DatabasePlatform platform;

  private Migration dbMigration;

  private DdlWrite write;

  int changeSetCount;

  public ModelDdlWriter(DatabasePlatform platform, ServerConfig serverConfig) {
    this.platform = platform;
    this.serverConfig = serverConfig;
  }

  public boolean processMigration(Migration dbMigration, DdlWrite write) throws IOException {

    this.changeSetCount = 0;
    this.dbMigration = dbMigration;
    this.write = write;

    DdlHandler handler = handler();

    List<ChangeSet> changeSets = dbMigration.getChangeSet();
    for (ChangeSet changeSet : changeSets) {
      if (!changeSet.getChangeSetChildren().isEmpty()) {
        changeSetCount++;
        handler.generate(write, changeSet);
      }
    }
    handler.generateExtra(write);

    return changeSetCount > 0;
  }

  /**
   * Write as migration xml to the given file.
   */
  public void writeMigration(File resourcePath, int migrationVersion) throws IOException {

    File file = new File(resourcePath, "v"+migrationVersion+".0.xml");

    MigrationXmlWriter xmlWriter = new MigrationXmlWriter();
    xmlWriter.write(dbMigration, file);

    if (!write.isApplyEmpty()) {
      FileWriter ddlWriter = createWriter(resourcePath, migrationVersion, ".0-apply.sql");
      try {
        writeApplyDdl(ddlWriter);
        ddlWriter.flush();
      } finally {
        ddlWriter.close();
      }

      FileWriter rbWriter = createWriter(resourcePath, migrationVersion, ".0-applyRollback.sql");
      try {
        writeApplyRollbackDdl(rbWriter);
        rbWriter.flush();
      } finally {
        rbWriter.close();
      }
    }

    String content = write.drop().getBuffer();
    if (!content.isEmpty()) {
      writeFile(resourcePath, migrationVersion, ".0-drop.sql", content);
    }

    String dropHistory = write.dropHistory().getBuffer();
    if (!dropHistory.isEmpty()) {
      writeFile(resourcePath, migrationVersion, ".0-dropHistory.sql", dropHistory);
    }
  }

  private FileWriter createWriter(File resourcePath, int migrationVersion, String suffix) throws IOException {

    File applyFile = new File(resourcePath, "v" + migrationVersion + suffix);
    return new FileWriter(applyFile);
  }

  private void writeFile(File resourcePath, int migrationVersion, String suffix, String content) throws IOException {

    FileWriter ddlWriter =createWriter(resourcePath, migrationVersion, suffix);
    try {
      ddlWriter.append(content);
      ddlWriter.flush();
    } finally {
      ddlWriter.close();
    }
  }


  /**
   * Return the 'Create' DDL.
   */
  private void writeApplyDdl(Writer writer) throws IOException {

    writer.append(write.apply().getBuffer());
    writer.append(write.applyForeignKeys().getBuffer());
    writer.append(write.applyHistory().getBuffer());
  }

  /**
   * Return the 'Rollback' DDL.
   */
  private void writeApplyRollbackDdl(Writer writer) throws IOException {

    writer.append(write.rollbackForeignKeys().getBuffer());
    writer.append(write.rollback().getBuffer());
  }

  /**
   * Return the platform specific DdlHandler (to generate DDL).
   */
  private DdlHandler handler() {
    return platform.createDdlHandler(serverConfig);
  }

}

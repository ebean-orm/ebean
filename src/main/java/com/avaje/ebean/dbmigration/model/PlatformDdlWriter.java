package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.Migration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Writes migration changes as platform specific DDL.
 */
public class PlatformDdlWriter {

  private final ServerConfig serverConfig;

  private final DatabasePlatform platform;

  private final String platformPrefix;

  private final boolean useSubdirectories;

  public PlatformDdlWriter(DatabasePlatform platform, ServerConfig serverConfig, String platformPrefix, boolean useSubdirectories) {
    this.platform = platform;
    this.serverConfig = serverConfig;
    this.platformPrefix = platformPrefix;
    this.useSubdirectories = useSubdirectories;
  }

  /**
   * Write the migration as platform specific ddl.
   */
  public void processMigration(Migration dbMigration, DdlWrite write, File writePath, String fullVersion) throws IOException {

    DdlHandler handler = handler();

    List<ChangeSet> changeSets = dbMigration.getChangeSet();
    for (ChangeSet changeSet : changeSets) {
      if (!changeSet.getChangeSetChildren().isEmpty()) {
        handler.generate(write, changeSet);
      }
    }
    handler.generateExtra(write);

    writePlatformDdl(write, writePath, fullVersion);
  }

  /**
   * Write the ddl files.
   */
  protected void writePlatformDdl(DdlWrite write, File resourcePath, String fullVersion) throws IOException {

    if (!write.isApplyEmpty()) {
      FileWriter applyWriter = createWriter(resourcePath, fullVersion, "");
      try {
        writeApplyDdl(applyWriter, write);
        applyWriter.flush();
      } finally {
        applyWriter.close();
      }

      if (!write.isApplyRollbackEmpty()) {
        FileWriter applyRollbackWriter = createWriter(resourcePath, fullVersion, "rollback");
        try {
          writeApplyRollbackDdl(applyRollbackWriter, write);
          applyRollbackWriter.flush();
        } finally {
          applyRollbackWriter.close();
        }
      }
    }

    if (!write.isDropEmpty()) {
      FileWriter dropWriter = createWriter(resourcePath, fullVersion, "drop");
      try {
        writeDropDdl(dropWriter, write);
        dropWriter.flush();
      } finally {
        dropWriter.close();
      }
    }
  }

  protected FileWriter createWriter(File path, String fullVersion, String suffix) throws IOException {

    String fileName = fullVersion;
    if (!platformPrefix.isEmpty()) {
      fileName += "-"+platformPrefix;
    }
    if (!suffix.isEmpty()) {
      fileName += "-"+suffix;
      path = subPath(path, suffix);
    }
    fileName += ".sql";
    File applyFile = new File(path,  fileName);
    return new FileWriter(applyFile);
  }

  protected File subPath(File path, String suffix) {
    if (!useSubdirectories) {
      return path;
    }
    File subPath = new File(path, suffix);
    if (!subPath.exists()) {
      subPath.mkdirs();
    }
    return subPath;
  }

  /**
   * Write the 'Apply' DDL buffers to the writer.
   */
  protected void writeApplyDdl(Writer writer, DdlWrite write) throws IOException {

    // merge the apply buffers in the appropriate order
    writer.append(write.apply().getBuffer());
    writer.append(write.applyForeignKeys().getBuffer());
    writer.append(write.applyHistory().getBuffer());
  }

  /**
   * Write the 'Rollback' DDL buffers to the writer.
   */
  protected void writeApplyRollbackDdl(Writer writer, DdlWrite write) throws IOException {

    // merge the rollback buffers in the appropriate order
    writer.append(write.rollbackForeignKeys().getBuffer());
    writer.append(write.rollback().getBuffer());
  }

  /**
   * Write the 'Drop' DDL buffers to the writer.
   */
  protected void writeDropDdl(Writer writer, DdlWrite write) throws IOException {

    // merge the rollback buffers in the appropriate order
    writer.append(write.dropHistory().getBuffer());
    writer.append(write.drop().getBuffer());
  }

  /**
   * Return the platform specific DdlHandler (to generate DDL).
   */
  protected DdlHandler handler() {
    return platform.createDdlHandler(serverConfig);
  }

}

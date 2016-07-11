package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.config.DbMigrationConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.ChangeSetType;
import com.avaje.ebean.dbmigration.migration.Migration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Writes migration changes as platform specific DDL.
 */
public class PlatformDdlWriter {

  private static final Logger logger = LoggerFactory.getLogger(PlatformDdlWriter.class);

  private final ServerConfig serverConfig;

  private final DatabasePlatform platform;

  private final String platformPrefix;

  private final DbMigrationConfig config;

  public PlatformDdlWriter(DatabasePlatform platform, ServerConfig serverConfig, String platformPrefix, DbMigrationConfig config) {
    this.platform = platform;
    this.serverConfig = serverConfig;
    this.platformPrefix = platformPrefix;
    this.config = config;
  }

  /**
   * Write the migration as platform specific ddl.
   */
  public void processMigration(Migration dbMigration, DdlWrite write, File writePath, String fullVersion) throws IOException {

    DdlHandler handler = handler();

    List<ChangeSet> changeSets = dbMigration.getChangeSet();
    for (ChangeSet changeSet : changeSets) {
      if (isApply(changeSet)) {
        handler.generate(write, changeSet);
      }
    }
    handler.generateExtra(write);

    writePlatformDdl(write, writePath, fullVersion);
  }

  /**
   * Return true if the changeSet is APPLY and not empty.
   */
  private boolean isApply(ChangeSet changeSet) {
    return changeSet.getType() == ChangeSetType.APPLY && !changeSet.getChangeSetChildren().isEmpty();
  }

  /**
   * Write the ddl files.
   */
  protected void writePlatformDdl(DdlWrite write, File resourcePath, String fullVersion) throws IOException {

    if (!write.isApplyEmpty()) {
      FileWriter applyWriter = createWriter(resourcePath, fullVersion, config.getApplySuffix());
      try {
        writeApplyDdl(applyWriter, write);
        applyWriter.flush();
      } finally {
        applyWriter.close();
      }
    }
  }

  protected FileWriter createWriter(File path, String fullVersion, String suffix) throws IOException {

    File applyFile = new File(path,  fullVersion + suffix);
    return new FileWriter(applyFile);
  }

  /**
   * Write the 'Apply' DDL buffers to the writer.
   */
  protected void writeApplyDdl(Writer writer, DdlWrite write) throws IOException {

    // merge the apply buffers in the appropriate order
    prependDropDependencies(writer, write.applyDropDependencies());
    writer.append("-- apply changes\n");
    writer.append(write.apply().getBuffer());
    writer.append(write.applyForeignKeys().getBuffer());
    writer.append(write.applyHistory().getBuffer());
  }

  private void prependDropDependencies(Writer writer, DdlBuffer buffer) throws IOException {
    if (!buffer.isEmpty()) {
      writer.append("-- drop dependencies\n");
      writer.append(buffer.getBuffer());
      writer.append("\n");
    }
  }

  /**
   * Return the platform specific DdlHandler (to generate DDL).
   */
  protected DdlHandler handler() {
    return platform.createDdlHandler(serverConfig);
  }

  /**
   * Return a sub directory (for multi-platform ddl generation).
   */
  public File subPath(File path, String suffix) {
    File subPath = new File(path, suffix);
    if (!subPath.exists()) {
      if (!subPath.mkdirs()) {
        logger.error("failed to create directories for " + subPath.getAbsolutePath());
      }
    }
    return subPath;
  }

}

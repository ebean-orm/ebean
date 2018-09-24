package io.ebeaninternal.dbmigration.model;

import io.ebean.config.DbMigrationConfig;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlHandler;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import io.ebeaninternal.dbmigration.migration.ChangeSet;
import io.ebeaninternal.dbmigration.migration.ChangeSetType;
import io.ebeaninternal.dbmigration.migration.Migration;
import io.ebeaninternal.server.core.PlatformDdlBuilder;
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

  private final DbMigrationConfig config;

  private final PlatformDdl platformDdl;

  public PlatformDdlWriter(DatabasePlatform platform, ServerConfig serverConfig, DbMigrationConfig config) {
    this.platformDdl = PlatformDdlBuilder.create(platform);
    this.serverConfig = serverConfig;
    this.config = config;
  }

  /**
   * Write the migration as platform specific ddl.
   */
  public void processMigration(Migration dbMigration, DdlWrite write, File writePath, String fullVersion) throws IOException {

    DdlHandler handler = handler();
    handler.generateProlog(write);
    List<ChangeSet> changeSets = dbMigration.getChangeSet();
    for (ChangeSet changeSet : changeSets) {
      if (isApply(changeSet)) {
        handler.generate(write, changeSet);
      }
    }
    handler.generateEpilog(write);

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
      try (FileWriter applyWriter = createWriter(resourcePath, fullVersion, config.getApplySuffix())) {
        writeApplyDdl(applyWriter, write);
        applyWriter.flush();
      }
    }
  }

  protected FileWriter createWriter(File path, String fullVersion, String suffix) throws IOException {

    File applyFile = new File(path, fullVersion + suffix);
    return new FileWriter(applyFile);
  }

  /**
   * Write the 'Apply' DDL buffers to the writer.
   */
  protected void writeApplyDdl(Writer writer, DdlWrite write) throws IOException {

    String header = config.getDdlHeader();
    if (header != null) {
      writer.append(header).append('\n');
    }
    // merge the apply buffers in the appropriate order
    prependDropDependencies(writer, write.applyDropDependencies());
    writer.append("-- apply changes\n");
    writer.append(write.apply().getBuffer());
    writer.append(write.applyForeignKeys().getBuffer());
    writer.append(write.applyHistoryView().getBuffer());
    writer.append(write.applyHistoryTrigger().getBuffer());
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
    return platformDdl.createDdlHandler(serverConfig);
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

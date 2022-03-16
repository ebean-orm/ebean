package io.ebeaninternal.dbmigration.model;

import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.util.IOUtils;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlHandler;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import io.ebeaninternal.dbmigration.migration.ChangeSet;
import io.ebeaninternal.dbmigration.migration.ChangeSetType;
import io.ebeaninternal.dbmigration.migration.Migration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Writes migration changes as platform specific DDL.
 */
public class PlatformDdlWriter {

  private static final Logger logger = LoggerFactory.getLogger(PlatformDdlWriter.class);

  private final DatabaseConfig databaseConfig;
  private final PlatformDdl platformDdl;
  private final int lockTimeoutSeconds;

  public PlatformDdlWriter(DatabasePlatform platform, DatabaseConfig dbConfig, int lockTimeoutSeconds) {
    this.platformDdl = PlatformDdlBuilder.create(platform);
    this.databaseConfig = dbConfig;
    this.lockTimeoutSeconds = lockTimeoutSeconds;
  }

  /**
   * Write the migration as platform specific ddl.
   */
  public void processMigration(Migration dbMigration, DdlWrite writer, File writePath, String fullVersion)
    throws IOException {
    DdlHandler handler = handler();
    handler.generateProlog(writer);
    if (lockTimeoutSeconds > 0) {
      String lockSql = platformDdl.setLockTimeout(lockTimeoutSeconds);
      if (lockSql != null) {
        writer.apply().append(lockSql).endOfStatement().newLine();
      }
    }
    List<ChangeSet> changeSets = dbMigration.getChangeSet();
    for (ChangeSet changeSet : changeSets) {
      if (isApply(changeSet)) {
        handler.generate(writer, changeSet);
      }
    }
    handler.generateEpilog(writer);
    writePlatformDdl(writer, writePath, fullVersion);
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
  protected void writePlatformDdl(DdlWrite writer, File resourcePath, String fullVersion) throws IOException {
    if (!writer.isApplyEmpty()) {
      try (Writer applyWriter = createWriter(resourcePath, fullVersion, ".sql")) {
        writeApplyDdl(applyWriter, writer);
        applyWriter.flush();
      }
    }
  }

  protected Writer createWriter(File path, String fullVersion, String suffix) throws IOException {
    File applyFile = new File(path, fullVersion + suffix);
    return IOUtils.newWriter(applyFile);
  }

  /**
   * Write the 'Apply' DDL buffers to the writer.
   */
  protected void writeApplyDdl(Writer writer, DdlWrite ddl) throws IOException {
    String header = databaseConfig.getDdlHeader();
    if (header != null && !header.isEmpty()) {
      writer.append(header).append('\n');
    }
    ddl.writeApply(writer);
  }

  /**
   * Return the platform specific DdlHandler (to generate DDL).
   */
  protected DdlHandler handler() {
    return platformDdl.createDdlHandler(databaseConfig);
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

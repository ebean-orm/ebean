package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.ddlgeneration.DdlHandler;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.Migration;
import com.avaje.ebean.dbmigration.migrationreader.MigrationXmlWriter;
import com.avaje.ebean.dbmigration.model.build.ModelBuildBeanVisitor;
import com.avaje.ebean.dbmigration.model.build.ModelBuildContext;
import com.avaje.ebean.dbmigration.model.visitor.VisitAllUsing;
import com.avaje.ebeaninternal.api.SpiEbeanServer;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Reads EbeanServer bean descriptors to build the current model.
 */
public class CurrentModel {

  private final SpiEbeanServer server;

  private ModelContainer model;

  private ChangeSet changeSet;

  private DdlWrite write;

  /**
   * Construct with a given EbeanServer instance.
   */
  public CurrentModel(SpiEbeanServer server) {
    this.server = server;
  }

  /**
   * Return the current model by reading all the bean descriptors and properties.
   */
  public ModelContainer read() {
    if (model == null) {
      model = new ModelContainer();
      ModelBuildContext context = new ModelBuildContext(model);
      ModelBuildBeanVisitor visitor = new ModelBuildBeanVisitor(context);
      VisitAllUsing visit = new VisitAllUsing(visitor, server);
      visit.visitAllBeans();
    }
    return model;
  }

  public void setChangeSet(ChangeSet changeSet) {
   this.changeSet = changeSet;
  }

  /**
   * Return as a ChangeSet.
   */
  public ChangeSet getChangeSet() {
    read();
    if (changeSet == null) {
      changeSet = asChangeSet();
    }
    return changeSet;
  }

  /**
   * Write as migration xml to the given file.
   */
  public void writeMigration(File file) {

    ChangeSet changeSet = getChangeSet();
    Migration migration = new Migration();
    migration.getChangeSet().add(changeSet);

    MigrationXmlWriter writer = new MigrationXmlWriter();
    writer.write(migration, file);
  }

  /**
   * Return the 'Create' DDL.
   */
  public String getCreateDdl() throws IOException {

    createDdl();

    StringBuilder ddl = new StringBuilder(2000);
    ddl.append(write.apply().getBuffer());
    ddl.append(write.applyForeignKeys().getBuffer());
    ddl.append(write.applyHistory().getBuffer());

    return ddl.toString();
  }

  /**
   * Return the 'Drop' DDL.
   */
  public String getDropDdl() throws IOException {

    createDdl();

    StringBuilder ddl = new StringBuilder(2000);
    ddl.append(write.rollbackForeignKeys().getBuffer());
    ddl.append(write.rollback().getBuffer());

    return ddl.toString();
  }

  /**
   * Create all the DDL based on the changeSet.
   */
  private void createDdl() throws IOException {

    if (write == null) {
      ChangeSet createChangeSet = getChangeSet();

      write = new DdlWrite();

      DdlHandler handler = handler();
      handler.generate(write, createChangeSet);
    }
  }

  /**
   * Return the platform specific DdlHandler (to generate DDL).
   */
  private DdlHandler handler() {
    return server.getDatabasePlatform().createDdlHandler();
  }

  /**
   * Convert the model into a ChangeSet.
   */
  private ChangeSet asChangeSet() {

    // empty diff so changes will effectively all be create
    ModelDiff diff = new ModelDiff();
    diff.compareTo(model);

    List<Object> createChanges = diff.getCreateChanges();

    // put the changes into a ChangeSet
    ChangeSet createChangeSet = new ChangeSet();
    createChangeSet.getChangeSetChildren().addAll(createChanges);
    return createChangeSet;
  }

}

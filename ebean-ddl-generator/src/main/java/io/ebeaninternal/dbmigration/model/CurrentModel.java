package io.ebeaninternal.dbmigration.model;

import io.ebean.config.DbConstraintNaming;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.dbmigration.Detect;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlHandler;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlOptions;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.ChangeSet;
import io.ebeaninternal.dbmigration.model.build.ModelBuildBeanVisitor;
import io.ebeaninternal.dbmigration.model.build.ModelBuildContext;
import io.ebeaninternal.dbmigration.model.visitor.VisitAllUsing;
import io.ebeaninternal.extraddl.model.DdlScript;
import io.ebeaninternal.extraddl.model.ExtraDdl;
import io.ebeaninternal.extraddl.model.ExtraDdlXmlReader;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;

import java.io.IOException;
import java.util.List;

import static io.ebeaninternal.api.PlatformMatch.matchPlatform;

/**
 * Reads EbeanServer bean descriptors to build the current model.
 */
public class CurrentModel {

  private final SpiEbeanServer server;

  private final DatabasePlatform databasePlatform;

  private final DbConstraintNaming constraintNaming;

  private final boolean platformTypes;

  private final boolean jaxbPresent;

  private final String ddlHeader;

  private ModelContainer model;

  private ChangeSet changeSet;

  private DdlWrite write;

  private DdlOptions ddlOptions = new DdlOptions();

  /**
   * Construct with a given EbeanServer instance for DDL create all generation, not migration.
   */
  public CurrentModel(SpiEbeanServer server) {
    this(server, server.getServerConfig().getConstraintNaming(), true);
  }

  /**
   * Construct with a given EbeanServer, platformDdl and constraintNaming convention.
   * <p>
   * Note the EbeanServer is just used to read the BeanDescriptors and platformDdl supplies
   * the platform specific handling on
   * </p>
   */
  public CurrentModel(SpiEbeanServer server, DbConstraintNaming constraintNaming) {
    this(server, constraintNaming, false);
  }

  private CurrentModel(SpiEbeanServer server, DbConstraintNaming constraintNaming, boolean platformTypes) {
    this.server = server;
    this.databasePlatform = server.getDatabasePlatform();
    this.constraintNaming = constraintNaming;
    this.platformTypes = platformTypes;
    this.ddlHeader = server.getServerConfig().getDdlHeader();
    this.jaxbPresent = Detect.isJAXBPresent(server.getServerConfig());
  }

  public DdlOptions getDdlOptions() {
    return ddlOptions;
  }

  /**
   * Return true if the model contains tables that are partitioned.
   */
  public boolean isTablePartitioning() {
    return read().isTablePartitioning();
  }

  /**
   * Return the tables that have partitioning.
   */
  public List<MTable> getPartitionedTables() {
    return read().getPartitionedTables();
  }

  /**
   * Return the current model by reading all the bean descriptors and properties.
   */
  public ModelContainer read() {
    if (model == null) {
      model = new ModelContainer();
      ModelBuildContext context = new ModelBuildContext(model, databasePlatform, constraintNaming, platformTypes);
      ModelBuildBeanVisitor visitor = new ModelBuildBeanVisitor(context);
      VisitAllUsing visit = new VisitAllUsing(visitor, server);
      visit.visitAllBeans();
      // adjust the foreign keys on the 'draft' tables
      context.adjustDraftReferences();
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
   * Return the 'Create' DDL.
   */
  public String getCreateDdl() throws IOException {

    createDdl();

    StringBuilder ddl = new StringBuilder(2000);
    if (ddlHeader != null && !ddlHeader.isEmpty()) {
      ddl.append(ddlHeader).append('\n');
    }
    if (jaxbPresent) {
      addExtraDdl(ddl, ExtraDdlXmlReader.readBuiltin(), "-- init script ");
    }
    ddl.append(write.apply().getBuffer());
    ddl.append(write.applyForeignKeys().getBuffer());
    ddl.append(write.applyHistoryView().getBuffer());
    ddl.append(write.applyHistoryTrigger().getBuffer());
    return ddl.toString();
  }

  private void addExtraDdl(StringBuilder ddl, ExtraDdl extraDdl, String prefix) {
    if (extraDdl != null) {
      List<DdlScript> ddlScript = extraDdl.getDdlScript();
      for (DdlScript script : ddlScript) {
        if (script.isInit() && matchPlatform(server.getDatabasePlatform().getPlatform(), script.getPlatforms())) {
          ddl.append(prefix).append(script.getName()).append('\n');
          ddl.append(script.getValue());
        }
      }
    }
  }

  /**
   * Return the 'Drop' DDL.
   */
  public String getDropAllDdl() throws IOException {

    createDdl();

    StringBuilder ddl = new StringBuilder(2000);
    if (ddlHeader != null && !ddlHeader.isEmpty()) {
      ddl.append(ddlHeader).append('\n');
    }
    ddl.append(write.dropAllForeignKeys().getBuffer());
    ddl.append(write.dropAll().getBuffer());
    return ddl.toString();
  }

  /**
   * Create all the DDL based on the changeSet.
   */
  private void createDdl() throws IOException {
    if (write == null) {
      ChangeSet createChangeSet = getChangeSet();
      write = new DdlWrite(new MConfiguration(), model, ddlOptions);
      DdlHandler handler = handler();
      handler.generateProlog(write);
      handler.generate(write, createChangeSet);
      handler.generateEpilog(write);
    }
  }

  /**
   * Return the platform specific DdlHandler (to generate DDL).
   */
  private DdlHandler handler() {
    return PlatformDdlBuilder.create(databasePlatform).createDdlHandler(server.getServerConfig());
  }

  /**
   * Convert the model into a ChangeSet.
   */
  private ChangeSet asChangeSet() {
    // empty diff so changes will effectively all be create
    ModelDiff diff = new ModelDiff();
    diff.compareTo(model);
    return diff.getApplyChangeSet();
  }

}

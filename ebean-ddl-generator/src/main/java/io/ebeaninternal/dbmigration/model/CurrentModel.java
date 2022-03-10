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
  private final DdlOptions ddlOptions = new DdlOptions();

  private ModelContainer model;
  private ChangeSet changeSet;
  private DdlWrite writer;

  /**
   * Construct with a given EbeanServer instance for DDL create all generation, not migration.
   */
  public CurrentModel(SpiEbeanServer server) {
    this(server, server.config().getConstraintNaming(), true);
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
    this.databasePlatform = server.databasePlatform();
    this.constraintNaming = constraintNaming;
    this.platformTypes = platformTypes;
    this.ddlHeader = server.config().getDdlHeader();
    this.jaxbPresent = Detect.isJAXBPresent(server.config());
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
  public String getCreateDdl() {

    createDdl();

    StringBuilder ddl = new StringBuilder(2000);
    if (ddlHeader != null && !ddlHeader.isEmpty()) {
      ddl.append(ddlHeader).append('\n');
    }
    if (jaxbPresent) {
      addExtraDdl(ddl, ExtraDdlXmlReader.readBuiltin(), "-- init script ");
    }

    try {
      writer.writeApply(ddl);
    } catch (IOException e) { // should not happen on StringBuilder
      throw new RuntimeException(e);
    }

    return ddl.toString();
  }

  private void addExtraDdl(StringBuilder ddl, ExtraDdl extraDdl, String prefix) {
    if (extraDdl != null) {
      List<DdlScript> ddlScript = extraDdl.getDdlScript();
      for (DdlScript script : ddlScript) {
        if (script.isInit() && matchPlatform(server.platform(), script.getPlatforms())) {
          ddl.append(prefix).append(script.getName()).append('\n');
          ddl.append(script.getValue());
        }
      }
    }
  }

  /**
   * Return the 'Drop' DDL.
   */
  public String getDropAllDdl() {

    createDdl();

    StringBuilder ddl = new StringBuilder(2000);
    if (ddlHeader != null && !ddlHeader.isEmpty()) {
      ddl.append(ddlHeader).append('\n');
    }
    try {
      writer.writeDropAll(ddl);
    } catch (IOException e) { // should not happen on StringBuilder
      throw new RuntimeException(e);
    }
    return ddl.toString();
  }

  /**
   * Create all the DDL based on the changeSet.
   */
  private void createDdl() {
    if (writer == null) {
      ChangeSet createChangeSet = getChangeSet();
      writer = new DdlWrite(new MConfiguration(), model, ddlOptions);
      DdlHandler handler = handler();
      handler.generateProlog(writer);
      handler.generate(writer, createChangeSet);
      handler.generateEpilog(writer);
    }
  }

  /**
   * Return the platform specific DdlHandler (to generate DDL).
   */
  private DdlHandler handler() {
    return PlatformDdlBuilder.create(databasePlatform).createDdlHandler(server.config());
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

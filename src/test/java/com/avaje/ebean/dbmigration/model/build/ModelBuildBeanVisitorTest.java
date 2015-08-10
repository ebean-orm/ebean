package com.avaje.ebean.dbmigration.model.build;


import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.DbConstraintNaming;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.PlatformDdl;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebean.dbmigration.model.MTable;
import com.avaje.ebean.dbmigration.model.ModelContainer;
import com.avaje.ebean.dbmigration.model.visitor.VisitAllUsing;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelBuildBeanVisitorTest extends BaseTestCase {

  @Test
  public void test() {

    SpiEbeanServer defaultServer = (SpiEbeanServer)Ebean.getDefaultServer();

    ModelContainer model = new ModelContainer();

    PlatformDdl platformDdl = defaultServer.getDatabasePlatform().getPlatformDdl();
    DbConstraintNaming constraintNaming = defaultServer.getServerConfig().getConstraintNaming();
    ModelBuildContext ctx = new ModelBuildContext(model, constraintNaming, platformDdl);

    ModelBuildBeanVisitor addTable = new ModelBuildBeanVisitor(ctx);

    new VisitAllUsing(addTable, defaultServer).visitAllBeans();

    MTable item = model.getTable("item");

    assertThat(item).isNotNull();
    assertThat(item.primaryKeyColumns()).hasSize(2);

    MTable customer = model.getTable("o_customer");

    assertThat(customer).isNotNull();
    assertThat(customer.getSequenceName()).isNull();
  }
}
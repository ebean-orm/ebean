package io.ebeaninternal.dbmigration.model.build;


import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.config.DbConstraintNaming;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.DefaultConstraintMaxLength;
import io.ebeaninternal.dbmigration.model.MColumn;
import io.ebeaninternal.dbmigration.model.MTable;
import io.ebeaninternal.dbmigration.model.ModelContainer;
import io.ebeaninternal.dbmigration.model.visitor.VisitAllUsing;
import io.ebeaninternal.api.SpiEbeanServer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelBuildBeanVisitorTest extends BaseTestCase {

  @Test
  public void test() {

    SpiEbeanServer defaultServer = (SpiEbeanServer) Ebean.getDefaultServer();

    ModelContainer model = new ModelContainer();

    DbConstraintNaming constraintNaming = defaultServer.getServerConfig().getConstraintNaming();

    DefaultConstraintMaxLength maxLength = new DefaultConstraintMaxLength(60);
    ModelBuildContext ctx = new ModelBuildContext(model, constraintNaming, maxLength, true);

    ModelBuildBeanVisitor addTable = new ModelBuildBeanVisitor(ctx);

    new VisitAllUsing(addTable, defaultServer).visitAllBeans();

    assert_compound_pk(model);

    assert_discriminatorColumn_explicit(model);
    assert_discriminatorColumn_implied(model);
    assert_discriminatorColumn_length(model);
  }

  private void assert_compound_pk(ModelContainer model) {
    MTable item = model.getTable("item");

    assertThat(item).isNotNull();
    assertThat(item.primaryKeyColumns()).hasSize(2);
  }

  private void assert_discriminatorColumn_explicit(ModelContainer model) {

    MTable configuration = model.getTable("configuration");
    MColumn discTypeColumn = configuration.getColumn("type");
    assertThat(discTypeColumn.getType()).isEqualTo("varchar(21)");
    assertThat(discTypeColumn.isNotnull()).isTrue();
  }

  private void assert_discriminatorColumn_implied(ModelContainer model) {

    MTable configuration = model.getTable("bar");
    MColumn discTypeColumn = configuration.getColumn("bar_type");
    assertThat(discTypeColumn.getType()).isEqualTo("varchar(31)");
    assertThat(discTypeColumn.isNotnull()).isTrue();
  }

  private void assert_discriminatorColumn_length(ModelContainer model) {

    MTable configuration = model.getTable("vehicle");
    MColumn discTypeColumn = configuration.getColumn("dtype");
    assertThat(discTypeColumn.getType()).isEqualTo("varchar(3)");
    assertThat(discTypeColumn.isNotnull()).isTrue();
  }
}

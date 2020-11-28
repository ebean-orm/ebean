package io.ebeaninternal.dbmigration.model.build;


import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.DbConstraintNaming;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.DefaultConstraintMaxLength;
import io.ebeaninternal.dbmigration.model.MColumn;
import io.ebeaninternal.dbmigration.model.MCompoundForeignKey;
import io.ebeaninternal.dbmigration.model.MTable;
import io.ebeaninternal.dbmigration.model.ModelContainer;
import io.ebeaninternal.dbmigration.model.visitor.VisitAllUsing;
import org.junit.Test;
import org.tests.model.basic.CKeyAssoc;
import org.tests.model.basic.CKeyDetail;
import org.tests.model.basic.CKeyParent;
import org.tests.model.basic.CKeyParentId;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelBuildBeanVisitorTest extends BaseTestCase {

  @Test
  public void test() {

    SpiEbeanServer defaultServer = (SpiEbeanServer) DB.getDefault();

    ModelContainer model = new ModelContainer();

    DbConstraintNaming constraintNaming = defaultServer.getServerConfig().getConstraintNaming();
    constraintNaming.setMaxLength(new DefaultConstraintMaxLength(60));
    ModelBuildContext ctx = new ModelBuildContext(model, new H2Platform(), constraintNaming, true);

    ModelBuildBeanVisitor addTable = new ModelBuildBeanVisitor(ctx);

    new VisitAllUsing(addTable, defaultServer).visitAllBeans();

    assert_compound_pk(model);

    assert_discriminatorColumn_explicit(model);
    assert_discriminatorColumn_implied(model);
    assert_discriminatorColumn_length(model);
  }

  @Test
  public void test_allQuoted() {

    ModelContainer model = new ModelContainer();

    DatabaseConfig config = new DatabaseConfig();
    config.setName("h2");
    config.loadFromProperties();
    config.setName("h2other");
    config.setAllQuotedIdentifiers(true);
    config.setDdlGenerate(false);
    config.setDdlRun(false);
    config.setDdlExtra(false);
    config.setDefaultServer(false);
    config.setRegister(false);

    config.addClass(CKeyDetail.class);
    config.addClass(CKeyParent.class);
    config.addClass(CKeyAssoc.class);
    config.addClass(CKeyParentId.class);
    config.setDbOffline(true);
    config.setDatabasePlatform(new SqlServer17Platform());

    final SpiEbeanServer database = (SpiEbeanServer)DatabaseFactory.create(config);
    try {
      ModelBuildContext ctx = new ModelBuildContext(model, config.getDatabasePlatform(), config.getConstraintNaming(), true);

      ModelBuildBeanVisitor addTable = new ModelBuildBeanVisitor(ctx);

      new VisitAllUsing(addTable, database).visitAllBeans();

      MTable parent = model.getTable("[CKeyParent]");
      assertThat(parent.getPkName()).isEqualTo("[pk_CKeyParent]");

      MTable detail = model.getTable("[CKeyDetail]");
      assertThat(detail.getPkName()).isEqualTo("[pk_CKeyDetail]");

      final List<MCompoundForeignKey> compoundKeys = detail.getCompoundKeys();
      assertThat(compoundKeys).hasSize(1);
      final MCompoundForeignKey fkey = compoundKeys.get(0);
      assertThat(fkey.getName()).isEqualTo("[fk_CKeyDetail_parent]");
      assertThat(fkey.getIndexName()).isEqualTo("[ix_CKeyDetail_parent]");

    } finally {
      database.shutdown();
    }
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

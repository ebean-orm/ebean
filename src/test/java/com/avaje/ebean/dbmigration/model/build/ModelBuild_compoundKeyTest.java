package com.avaje.ebean.dbmigration.model.build;


import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.ddlgeneration.Helper;
import com.avaje.ebean.dbmigration.migration.Migration;
import com.avaje.ebean.dbmigration.migrationreader.MigrationXmlReader;
import com.avaje.ebean.dbmigration.model.CurrentModel;
import com.avaje.ebean.dbmigration.model.MTable;
import com.avaje.ebean.dbmigration.model.ModelContainer;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.tests.model.basic.CKeyAssoc;
import com.avaje.tests.model.basic.CKeyDetail;
import com.avaje.tests.model.basic.CKeyParent;
import com.avaje.tests.model.basic.CKeyParentId;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelBuild_compoundKeyTest extends BaseTestCase {

  private SpiEbeanServer getServer() {
    ServerConfig config = new ServerConfig();
    config.setName("h2");
    config.loadFromProperties();
    config.setName("h2other");
    config.setDdlGenerate(false);
    config.setDdlRun(false);
    config.setDefaultServer(false);
    config.setRegister(false);

    config.addClass(CKeyDetail.class);
    config.addClass(CKeyParent.class);
    config.addClass(CKeyAssoc.class);
    config.addClass(CKeyParentId.class);


    return (SpiEbeanServer) EbeanServerFactory.create(config);
  }

  @Test
  public void test() throws IOException {

    SpiEbeanServer ebeanServer = getServer();

    CurrentModel currentModel = new CurrentModel(ebeanServer);
    ModelContainer model = currentModel.read();

    MTable parent = model.getTable("ckey_parent");
    MTable detail = model.getTable("ckey_detail");

    assertThat(parent).isNotNull();
    assertThat(detail).isNotNull();
    assertThat(parent.primaryKeyColumns()).hasSize(2);
    assertThat(detail.getCompoundKeys()).hasSize(1);

    String apply = Helper.asText(this, "/assert/ModelBuild_compoundKeyTest/apply.sql");

    String createDdl = currentModel.getCreateDdl();
    assertThat(createDdl).isEqualTo(apply);

  }


  @Test
  public void testFromMigration() throws IOException {


    Migration migration = MigrationXmlReader.read("/container/test-compoundkey.xml");

    SpiEbeanServer ebeanServer = getServer();
    CurrentModel currentModel = new CurrentModel(ebeanServer);
    currentModel.setChangeSet(migration.getChangeSet().get(0));

    String createDdl = currentModel.getCreateDdl();
    String apply = Helper.asText(this, "/assert/ModelBuild_compoundKeyTest/apply.sql");

    assertThat(createDdl).isEqualTo(apply);
  }
}
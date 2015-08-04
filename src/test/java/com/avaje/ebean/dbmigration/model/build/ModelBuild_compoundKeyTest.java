package com.avaje.ebean.dbmigration.model.build;


import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.model.CurrentModel;
import com.avaje.ebean.dbmigration.model.MTable;
import com.avaje.ebean.dbmigration.model.ModelContainer;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.tests.model.basic.CKeyAssoc;
import com.avaje.tests.model.basic.CKeyDetail;
import com.avaje.tests.model.basic.CKeyParent;
import com.avaje.tests.model.basic.CKeyParentId;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelBuild_compoundKeyTest extends BaseTestCase {

  @Test
  public void test() {

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


    SpiEbeanServer ebeanServer = (SpiEbeanServer)EbeanServerFactory.create(config);

    CurrentModel currentModel = new CurrentModel(ebeanServer);
    ModelContainer model = currentModel.read();

    MTable parent = model.getTable("ckey_parent");
    MTable detail = model.getTable("ckey_detail");

    assertThat(parent).isNotNull();
    assertThat(detail).isNotNull();

  }
}
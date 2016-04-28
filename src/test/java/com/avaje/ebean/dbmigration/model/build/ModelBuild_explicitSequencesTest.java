package com.avaje.ebean.dbmigration.model.build;


import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.ddlgeneration.Helper;
import com.avaje.ebean.dbmigration.model.CurrentModel;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.tests.model.basic.Person;
import com.avaje.tests.model.basic.Phone;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelBuild_explicitSequencesTest extends BaseTestCase {

  private SpiEbeanServer getServer(boolean postgres) {

    System.setProperty("ebean.ignoreExtraDdl", "true");

    ServerConfig config = new ServerConfig();
    config.setName("h2");
    config.loadFromProperties();
    config.setName("h2other");
    config.setDdlGenerate(false);
    config.setDdlRun(false);
    config.setDefaultServer(false);
    config.setRegister(false);

    config.setDatabasePlatformName(postgres ? "postgres" : "h2");

    config.addClass(Person.class);
    config.addClass(Phone.class);

    return (SpiEbeanServer) EbeanServerFactory.create(config);
  }

  @Test
  public void test() throws IOException {

    SpiEbeanServer ebeanServer = getServer(false);
    CurrentModel currentModel = new CurrentModel(ebeanServer);

    String apply = currentModel.getCreateDdl();
    assertThat(apply).isEqualTo(Helper.asText(this, "/assert/ModelBuild_explicitSequencesTest/apply.sql"));

  }

  @Test
  public void test_asPostgres() throws IOException {

    SpiEbeanServer ebeanServer = getServer(true);
    CurrentModel currentModel = new CurrentModel(ebeanServer);

    String apply = currentModel.getCreateDdl();
    assertThat(apply).isEqualTo(Helper.asText(this, "/assert/ModelBuild_explicitSequencesTest/pg-apply.sql"));

  }


}
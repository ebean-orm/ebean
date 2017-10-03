package io.ebean.dbmigration.model.build;


import io.ebean.BaseTestCase;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import io.ebean.dbmigration.ddlgeneration.Helper;
import io.ebean.dbmigration.model.CurrentModel;
import io.ebean.plugin.SpiServer;
import org.tests.model.basic.Person;
import org.tests.model.basic.Phone;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelBuild_explicitSequencesTest extends BaseTestCase {

  private SpiServer getServer(boolean postgres) {

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

    return EbeanServerFactory.create(config).getPluginApi();
  }

  @Test
  public void test() throws IOException {

    SpiServer ebeanServer = getServer(false);
    CurrentModel currentModel = new CurrentModel(ebeanServer);

    String apply = currentModel.getCreateDdl();
    assertThat(apply).isEqualTo(Helper.asText(this, "/assert/ModelBuild_explicitSequencesTest/apply.sql"));

  }

  @Test
  public void test_asPostgres() throws IOException {

    SpiServer ebeanServer = getServer(true);
    CurrentModel currentModel = new CurrentModel(ebeanServer);

    String apply = currentModel.getCreateDdl();
    assertThat(apply).isEqualTo(Helper.asText(this, "/assert/ModelBuild_explicitSequencesTest/pg-apply.sql"));

  }


}

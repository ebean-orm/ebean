package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.dbplatform.h2.H2Platform;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.model.CurrentModel;
import com.avaje.ebean.dbmigration.model.MConfiguration;
import com.avaje.ebean.dbmigration.model.ModelContainer;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;


public class H2HistoryDdlTest {

  @Test
  public void testRegenerateHistoryTriggers() throws Exception {

    SpiEbeanServer ebeanServer = (SpiEbeanServer) Ebean.getDefaultServer();

    HistoryTableUpdate update = new HistoryTableUpdate("c_user");
    update.add(HistoryTableUpdate.Change.ADD, "one");
    update.add(HistoryTableUpdate.Change.DROP, "two");


    CurrentModel currentModel = new CurrentModel(ebeanServer);
    ModelContainer modelContainer = currentModel.read();
    DdlWrite write = new DdlWrite(new MConfiguration(), modelContainer);

    H2Platform h2Platform = new H2Platform();
    PlatformDdl h2Ddl = h2Platform.getPlatformDdl();
    h2Ddl.configure(ebeanServer.getServerConfig());
    h2Ddl.regenerateHistoryTriggers(write, update);

    assertThat(write.applyHistory().isEmpty()).isFalse();
    assertThat(write.applyHistory().getBuffer()).contains("add one");
    assertThat(write.dropAll().isEmpty()).isTrue();

  }
}

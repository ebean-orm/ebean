package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.DB;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlOptions;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;
import io.ebeaninternal.dbmigration.model.CurrentModel;
import io.ebeaninternal.dbmigration.model.MConfiguration;
import io.ebeaninternal.dbmigration.model.ModelContainer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class H2HistoryDdlTest {

  @Test
  public void testRegenerateHistoryTriggers() throws Exception {

    SpiEbeanServer ebeanServer = (SpiEbeanServer) DB.getDefault();

    HistoryTableUpdate update = new HistoryTableUpdate("c_user");
    update.add(HistoryTableUpdate.Change.ADD, "one");
    update.add(HistoryTableUpdate.Change.DROP, "two");


    CurrentModel currentModel = new CurrentModel(ebeanServer);
    ModelContainer modelContainer = currentModel.read();
    DdlWrite write = new DdlWrite(new MConfiguration(), modelContainer, new DdlOptions());

    H2Platform h2Platform = new H2Platform();
    PlatformDdl h2Ddl = PlatformDdlBuilder.create(h2Platform);
    h2Ddl.configure(ebeanServer.config());
    h2Ddl.regenerateHistoryTriggers(write, update);

    assertThat(write.applyHistoryView().isEmpty()).isFalse();
    assertThat(write.applyHistoryTrigger().isEmpty()).isFalse();
    assertThat(write.applyHistoryView().getBuffer())
      .contains("create view")
      .doesNotContain("create trigger");
    assertThat(write.applyHistoryTrigger().getBuffer())
      .contains("add one")
      .contains("create trigger")
      .doesNotContain("create view");
    assertThat(write.dropAll().isEmpty()).isTrue();

  }
}

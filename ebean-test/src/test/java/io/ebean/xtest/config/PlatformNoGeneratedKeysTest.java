package io.ebean.xtest.config;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.Transaction;
import io.ebean.annotation.Platform;
import io.ebean.DatabaseBuilder;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.DbIdentity;
import io.ebean.config.dbplatform.IdType;
import io.ebean.platform.h2.H2Platform;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicVer;
import org.tests.model.draftable.BasicDraftableBean;

import static org.assertj.core.api.Assertions.assertThat;

public class PlatformNoGeneratedKeysTest {

  static Database server = testH2Server();

  @AfterAll
  public static void shutdown() {
    server.shutdown();
  }

  @Test
  public void global_serverConfig_setDisableLazyLoading() {

    EBasicVer b0 = new EBasicVer("basic");
    b0.setDescription("some description");

    server.save(b0);

    EBasicVer found = server.find(EBasicVer.class)
      .select("name")
      .setId(b0.getId())
      .findOne();

    assertThat(found.getName()).isEqualTo("basic");
    assertThat(found.getDescription()).isNull();
  }

  @Test
  public void insertBatch_expect_noIdValuesFetched() {

    EBasicVer b0 = new EBasicVer("a");
    EBasicVer b1 = new EBasicVer("b");
    EBasicVer b2 = new EBasicVer("c");

    try (Transaction transaction = server.beginTransaction()) {
      transaction.setBatchMode(true);

      server.save(b0);
      server.save(b1);
      server.save(b2);

      transaction.commit();
    }

    assertThat(b0.getId()).isNull();
    assertThat(b1.getId()).isNull();
    assertThat(b2.getId()).isNull();

  }

  @Test
  public void insertNoBatch_expect_selectIdentity() {

    EBasicVer b0 = new EBasicVer("one");
    server.save(b0);

    assertThat(b0.getId()).isNotNull();


    BasicDraftableBean d0 = new BasicDraftableBean("done");
    server.save(d0);

    assertThat(d0.getId()).isNotNull();

    server.publish(BasicDraftableBean.class, d0.getId());

    BasicDraftableBean one = server.find(BasicDraftableBean.class, d0.getId());

    assertThat(one.getName()).isEqualTo("done");
    assertThat(one.isDraft()).isFalse();
  }

  private static Database testH2Server() {

    DatabaseConfig config = new DatabaseConfig();
    config.setName("h2_noGeneratedKeys");

    OtherH2Platform platform = new OtherH2Platform();
    DbIdentity dbIdentity = platform.dbIdentity();
    dbIdentity.setIdType(IdType.IDENTITY);
    dbIdentity.setSupportsIdentity(true);
    dbIdentity.setSupportsGetGeneratedKeys(false);
    dbIdentity.setSupportsSequence(false);
    dbIdentity.setSelectLastInsertedIdTemplate("select identity() --{table}");

    config.setDatabasePlatform(platform);
    config.getDataSourceConfig().username("sa");
    config.getDataSourceConfig().password("");
    config.getDataSourceConfig().url("jdbc:h2:mem:withPCQuery;MODE=LEGACY");
    config.getDataSourceConfig().driver("org.h2.Driver");

    config.setDisableLazyLoading(true);
    config.setDisableL2Cache(true);
    config.setDefaultServer(false);
    config.setRegister(false);
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.addClass(EBasicVer.class);
    config.addClass(BasicDraftableBean.class);
    config.loadFromProperties(); // trigger auto config for H2 1.x

    return DatabaseFactory.create(config);
  }

  public static class OtherH2Platform extends H2Platform {

    public OtherH2Platform() {
      super();
      this.platform = Platform.GENERIC;
    }
  }
}

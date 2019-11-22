package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PostgresDdlTest {

  private PostgresDdl postgresDdl = new PostgresDdl(new PostgresPlatform());

  @Test
  public void setLockTimeout() {

    final String sql = postgresDdl.setLockTimeout(5);
    assertThat(sql).isEqualTo("set lock_timeout = 5000");
  }
}

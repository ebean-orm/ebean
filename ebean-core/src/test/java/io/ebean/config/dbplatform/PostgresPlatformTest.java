package io.ebean.config.dbplatform;

import io.ebean.Query;
import io.ebean.config.PlatformConfig;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PostgresPlatformTest {

  @Test
  public void testUuidType() {

    PostgresPlatform platform = new PostgresPlatform();
    platform.configure(new PlatformConfig(), false);

    DbPlatformType dbType = platform.getDbTypeMap().get(DbPlatformType.UUID);
    String columnDefn = dbType.renderType(0, 0);

    assertThat(columnDefn).isEqualTo("uuid");
  }

  @Test
  public void default_forUpdate_expect_noKeyUsed() {

    DatabasePlatform platform = new PostgresPlatform();

    PlatformConfig config = new PlatformConfig();
    platform.configure(config);

    assertThat(config.isLockWithKey()).isTrue();
    assertThat(platform.withForUpdate("X", Query.ForUpdate.SKIPLOCKED)).isEqualTo("X for update skip locked");
    assertThat(platform.withForUpdate("X", Query.ForUpdate.NOWAIT)).isEqualTo("X for update nowait");
    assertThat(platform.withForUpdate("X", Query.ForUpdate.BASE)).isEqualTo("X for update");
  }

  @Test
  public void lockWithKey_forUpdate() {

    DatabasePlatform platform = new PostgresPlatform();

    PlatformConfig config = new PlatformConfig();
    config.setLockWithKey(false);
    platform.configure(config);

    assertThat(config.isLockWithKey()).isFalse();
    assertThat(platform.withForUpdate("X", Query.ForUpdate.SKIPLOCKED)).isEqualTo("X for no key update skip locked");
    assertThat(platform.withForUpdate("X", Query.ForUpdate.NOWAIT)).isEqualTo("X for no key update nowait");
    assertThat(platform.withForUpdate("X", Query.ForUpdate.BASE)).isEqualTo("X for no key update");
  }

}

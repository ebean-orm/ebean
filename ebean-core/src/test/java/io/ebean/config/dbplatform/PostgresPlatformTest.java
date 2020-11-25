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

    assertThat(config.isForUpdateNoKey()).isFalse();
    assertThat(platform.withForUpdate("X", Query.LockWait.SKIPLOCKED, Query.LockType.DEFAULT)).isEqualTo("X for update skip locked");
    assertThat(platform.withForUpdate("X", Query.LockWait.NOWAIT, Query.LockType.DEFAULT)).isEqualTo("X for update nowait");
    assertThat(platform.withForUpdate("X", Query.LockWait.WAIT, Query.LockType.DEFAULT)).isEqualTo("X for update");

    assertThat(platform.withForUpdate("X", Query.LockWait.SKIPLOCKED, Query.LockType.UPDATE)).isEqualTo("X for update skip locked");
    assertThat(platform.withForUpdate("X", Query.LockWait.SKIPLOCKED, Query.LockType.NO_KEY_UPDATE)).isEqualTo("X for no key update skip locked");
    assertThat(platform.withForUpdate("X", Query.LockWait.SKIPLOCKED, Query.LockType.SHARE)).isEqualTo("X for share skip locked");
    assertThat(platform.withForUpdate("X", Query.LockWait.SKIPLOCKED, Query.LockType.KEY_SHARE)).isEqualTo("X for key share skip locked");

    assertThat(platform.withForUpdate("X", Query.LockWait.NOWAIT, Query.LockType.UPDATE)).isEqualTo("X for update nowait");
    assertThat(platform.withForUpdate("X", Query.LockWait.NOWAIT, Query.LockType.NO_KEY_UPDATE)).isEqualTo("X for no key update nowait");
    assertThat(platform.withForUpdate("X", Query.LockWait.NOWAIT, Query.LockType.SHARE)).isEqualTo("X for share nowait");
    assertThat(platform.withForUpdate("X", Query.LockWait.NOWAIT, Query.LockType.KEY_SHARE)).isEqualTo("X for key share nowait");

    assertThat(platform.withForUpdate("X", Query.LockWait.WAIT, Query.LockType.UPDATE)).isEqualTo("X for update");
    assertThat(platform.withForUpdate("X", Query.LockWait.WAIT, Query.LockType.NO_KEY_UPDATE)).isEqualTo("X for no key update");
    assertThat(platform.withForUpdate("X", Query.LockWait.WAIT, Query.LockType.SHARE)).isEqualTo("X for share");
    assertThat(platform.withForUpdate("X", Query.LockWait.WAIT, Query.LockType.KEY_SHARE)).isEqualTo("X for key share");
  }

  @Test
  public void lockWithKey_forUpdate() {

    DatabasePlatform platform = new PostgresPlatform();

    PlatformConfig config = new PlatformConfig();
    config.setForUpdateNoKey(true);
    platform.configure(config);

    assertThat(config.isForUpdateNoKey()).isTrue();
    assertThat(platform.withForUpdate("X", Query.LockWait.SKIPLOCKED, Query.LockType.DEFAULT)).isEqualTo("X for no key update skip locked");
    assertThat(platform.withForUpdate("X", Query.LockWait.NOWAIT, Query.LockType.DEFAULT)).isEqualTo("X for no key update nowait");
    assertThat(platform.withForUpdate("X", Query.LockWait.WAIT, Query.LockType.DEFAULT)).isEqualTo("X for no key update");
  }

}

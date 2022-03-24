package io.ebean.xtest.config.dbplatform;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabasePlatformBaseTest extends BaseTestCase {

  @Test
  @ForPlatform(Platform.H2)
  public void h2_platform() {
    final Platform platform = DB.getDefault().platform();
    assertThat(platform).isSameAs(Platform.H2);
  }

  @Test
  @ForPlatform(Platform.POSTGRES)
  public void postgres_platform() {
    final Platform platform = DB.getDefault().platform().base();
    assertThat(platform).isSameAs(Platform.POSTGRES);
  }

  @Test
  @ForPlatform(Platform.MYSQL)
  public void mysql_platform() {
    final Platform platform = DB.getDefault().platform().base();
    assertThat(platform).isSameAs(Platform.MYSQL);
  }

  @Test
  @ForPlatform(Platform.MARIADB)
  public void mariadb_platform() {
    final Platform platform = DB.getDefault().platform().base();
    assertThat(platform).isSameAs(Platform.MARIADB);
  }

  @Test
  @ForPlatform(Platform.SQLSERVER)
  public void sqlserver_platform() {
    final Platform platform = DB.getDefault().platform().base();
    assertThat(platform).isSameAs(Platform.SQLSERVER);
  }

}

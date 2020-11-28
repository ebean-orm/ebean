package io.ebeaninternal.dbmigration;

import io.ebean.config.dbplatform.mysql.MySqlPlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MySqlPlatformTest {

  MySqlPlatform mySqlPlatform = new MySqlPlatform();

  @Test
  public void testTypeConversion() {
    PlatformDdl ddl = PlatformDdlBuilder.create(mySqlPlatform);
    assertThat(ddl.convert("clob")).isEqualTo("longtext");
    assertThat(ddl.convert("json")).isEqualTo("json");
    assertThat(ddl.convert("jsonb")).isEqualTo("json");
    assertThat(ddl.convert("varchar(20)")).isEqualTo("varchar(20)");
    assertThat(ddl.convert("boolean")).isEqualTo("tinyint(1)");
    assertThat(ddl.convert("bit")).isEqualTo("tinyint(1)");
    assertThat(ddl.convert("decimal")).isEqualTo("decimal(16,3)");
  }

}

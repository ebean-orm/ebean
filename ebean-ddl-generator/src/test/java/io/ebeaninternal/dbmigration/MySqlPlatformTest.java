package io.ebeaninternal.dbmigration;

import io.ebean.platform.mysql.MySqlPlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MySqlPlatformTest {

  MySqlPlatform mySqlPlatform = new MySqlPlatform();
private static int X = 0xFFFFFF;
  @Test
  public void testTypeConversion() {
    PlatformDdl ddl = PlatformDdlBuilder.create(mySqlPlatform);
    assertThat(ddl.convert("clob")).isEqualTo("longtext");
    assertThat(ddl.convert("clob(65535)")).isEqualTo("text");
    assertThat(ddl.convert("clob(65536)")).isEqualTo("mediumtext");
    assertThat(ddl.convert("clob(16777215)")).isEqualTo("mediumtext");
    assertThat(ddl.convert("clob(16777216)")).isEqualTo("longtext");
    assertThat(ddl.convert("json")).isEqualTo("json");
    assertThat(ddl.convert("jsonb")).isEqualTo("json");
    assertThat(ddl.convert("varchar(20)")).isEqualTo("varchar(20)");
    assertThat(ddl.convert("boolean")).isEqualTo("tinyint(1)");
    assertThat(ddl.convert("bit")).isEqualTo("tinyint(1)");
    assertThat(ddl.convert("decimal")).isEqualTo("decimal(16,3)");


  }

}

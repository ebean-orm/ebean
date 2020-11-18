package io.ebeaninternal.dbmigration;

import io.ebean.config.dbplatform.oracle.OraclePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OraclePlatformTest {

  OraclePlatform platform = new OraclePlatform();

  @Test
  public void testTypeConversion() {

    PlatformDdl ddl = PlatformDdlBuilder.create(platform);

    assertThat(ddl.convert("clob")).isEqualTo("clob");
    assertThat(ddl.convert("blob")).isEqualTo("blob");
    assertThat(ddl.convert("json")).isEqualTo("clob");
    assertThat(ddl.convert("jsonb")).isEqualTo("clob");

    assertThat(ddl.convert("double")).isEqualTo("number(19,4)");
    assertThat(ddl.convert("varchar(20)")).isEqualTo("varchar2(20)");
    assertThat(ddl.convert("decimal(10)")).isEqualTo("number(10)");
    assertThat(ddl.convert("decimal(8,4)")).isEqualTo("number(8,4)");
    assertThat(ddl.convert("decimal")).isEqualTo("number(16,3)");
    assertThat(ddl.convert("boolean")).isEqualTo("number(1)");
    assertThat(ddl.convert("bit")).isEqualTo("bit");
    assertThat(ddl.convert("tinyint")).isEqualTo("number(3)");
    assertThat(ddl.convert("binary(16)")).isEqualTo("raw(16)");
  }

}

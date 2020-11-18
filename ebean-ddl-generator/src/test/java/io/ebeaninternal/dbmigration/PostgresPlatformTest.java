package io.ebeaninternal.dbmigration;

import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PostgresPlatformTest {

  @Test
  public void testTypeConversion() {

    PostgresPlatform platform = new PostgresPlatform();
    PlatformDdl ddl = PlatformDdlBuilder.create(platform);

    assertThat(ddl.convert("clob")).isEqualTo("text");
    assertThat(ddl.convert("blob")).isEqualTo("bytea");
    assertThat(ddl.convert("json")).isEqualTo("json");
    assertThat(ddl.convert("jsonb")).isEqualTo("jsonb");
    assertThat(ddl.convert("hstore")).isEqualTo("hstore");
    assertThat(ddl.convert("double")).isEqualTo("float");
    assertThat(ddl.convert("tinyint")).isEqualTo("smallint");
    assertThat(ddl.convert("double")).isEqualTo("float");
    assertThat(ddl.convert("varchar(20)")).isEqualTo("varchar(20)");
    assertThat(ddl.convert("decimal")).isEqualTo("decimal(16,3)");
    assertThat(ddl.convert("decimal(10)")).isEqualTo("decimal(10)");
    assertThat(ddl.convert("decimal(8,4)")).isEqualTo("decimal(8,4)");
    assertThat(ddl.convert("boolean")).isEqualTo("boolean");
    assertThat(ddl.convert("bit")).isEqualTo("bit");
  }

}

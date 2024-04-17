package io.ebeaninternal.dbmigration;

import io.ebean.platform.clickhouse.ClickHousePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClickHousePlatformTest {

  @Test
  void testTypeConversion() {

    PlatformDdl ddl = PlatformDdlBuilder.create(new ClickHousePlatform());

    assertThat(ddl.convert("clob")).isEqualTo("String");
    assertThat(ddl.convert("varchar(20)")).isEqualTo("String");
    assertThat(ddl.convert("json")).isEqualTo("JSON");
    assertThat(ddl.convert("jsonb")).isEqualTo("JSON");
    assertThat(ddl.convert("decimal(10)")).isEqualTo("Decimal(10)");
    assertThat(ddl.convert("decimal(8,4)")).isEqualTo("Decimal(8,4)");
    assertThat(ddl.convert("decimal")).isEqualTo("Decimal(16,3)");
    assertThat(ddl.convert("boolean")).isEqualTo("Bool");
    assertThat(ddl.convert("bit")).isEqualTo("bit");
  }

}

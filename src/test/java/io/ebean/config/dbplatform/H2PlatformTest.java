package io.ebean.config.dbplatform;

import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.PlatformDdl;
import io.ebeaninternal.server.core.PlatformDdlBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class H2PlatformTest {

  @Test
  public void testTypeConversion() {
    
    PlatformDdl ddl = PlatformDdlBuilder.create(new H2Platform());

    assertThat(ddl.convert("clob", false)).isEqualTo("clob");
    assertThat(ddl.convert("json", false)).isEqualTo("clob");
    assertThat(ddl.convert("jsonb", false)).isEqualTo("clob");
    assertThat(ddl.convert("varchar(20)", false)).isEqualTo("varchar(20)");
    assertThat(ddl.convert("decimal(10)", false)).isEqualTo("decimal(10)");
    assertThat(ddl.convert("decimal(8,4)", false)).isEqualTo("decimal(8,4)");
    assertThat(ddl.convert("boolean", false)).isEqualTo("boolean");
    assertThat(ddl.convert("bit", false)).isEqualTo("bit");
  }

}

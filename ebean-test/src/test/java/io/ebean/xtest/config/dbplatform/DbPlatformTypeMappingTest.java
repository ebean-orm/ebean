package io.ebean.xtest.config.dbplatform;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbPlatformTypeMapping;
import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;

public class DbPlatformTypeMappingTest {

  @Test
  public void logicalBoolean_renderType_expect_noLength() {

    DbPlatformTypeMapping logicalMapping = DbPlatformTypeMapping.logicalTypes();

    DbPlatformType type = logicalMapping.get(Types.BOOLEAN);
    String colDefinition = type.renderType(1, 1, false);
    assertThat(colDefinition).isEqualTo("boolean");
  }
}

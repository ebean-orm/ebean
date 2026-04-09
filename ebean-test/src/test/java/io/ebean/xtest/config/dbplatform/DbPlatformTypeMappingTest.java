package io.ebean.xtest.config.dbplatform;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.DbPlatformTypeMapping;
import io.ebean.config.dbplatform.DbType;
import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;

class DbPlatformTypeMappingTest {

  @Test
  void logicalBoolean_renderType_expect_noLength() {

    DbPlatformTypeMapping logicalMapping = DbPlatformTypeMapping.logicalTypes();

    DbPlatformType type = logicalMapping.get(Types.BOOLEAN);
    String colDefinition = type.renderType(1, 1, false);
    assertThat(colDefinition).isEqualTo("boolean");
  }

  @Test
  void timestamp_with_255_expectNoPrecision() {
    DbPlatformType timestampType = new DbPlatformTypeMapping().get(DbType.TIMESTAMP);
    String colDefinition = timestampType.renderType(255, 0, true);
    assertThat(colDefinition).isEqualTo("timestamp");
  }
}

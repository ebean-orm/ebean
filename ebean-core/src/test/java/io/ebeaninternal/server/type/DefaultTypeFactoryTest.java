package io.ebeaninternal.server.type;

import io.ebean.config.DatabaseConfig;
import io.ebean.core.type.ScalarType;
import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class DefaultTypeFactoryTest {

  DefaultTypeFactory defaultTypeFactory = new DefaultTypeFactory(new DatabaseConfig());

  @Test
  public void testCreateBoolean() throws Exception {

    ScalarType<Boolean> stIntBoolean = defaultTypeFactory.createBoolean("0", "1");
    assertEquals(Types.INTEGER, stIntBoolean.jdbcType());

    stIntBoolean = defaultTypeFactory.createBoolean("1", "2");
    assertEquals(Types.INTEGER, stIntBoolean.jdbcType());

    ScalarType<Boolean> stStringBoolean = defaultTypeFactory.createBoolean("Y", "N");
    assertEquals(Types.VARCHAR, stStringBoolean.jdbcType());

  }
}

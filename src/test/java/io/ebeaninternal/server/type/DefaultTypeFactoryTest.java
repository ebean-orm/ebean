package io.ebeaninternal.server.type;

import org.junit.Test;

import java.sql.Types;

import static org.junit.Assert.assertEquals;


public class DefaultTypeFactoryTest {

  DefaultTypeFactory defaultTypeFactory = new DefaultTypeFactory(null);

  @Test
  public void testCreateBoolean() throws Exception {

    ScalarType<Boolean> stIntBoolean = defaultTypeFactory.createBoolean("0", "1");
    assertEquals(Types.INTEGER, stIntBoolean.getJdbcType());

    stIntBoolean = defaultTypeFactory.createBoolean("1", "2");
    assertEquals(Types.INTEGER, stIntBoolean.getJdbcType());

    ScalarType<Boolean> stStringBoolean = defaultTypeFactory.createBoolean("Y", "N");
    assertEquals(Types.VARCHAR, stStringBoolean.getJdbcType());

  }
}

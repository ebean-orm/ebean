package io.ebean.server.type;

import io.ebean.BaseTestCase;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.type.DefaultTypeManager;
import io.ebeaninternal.server.type.RsetDataReader;
import io.ebeaninternal.server.type.ScalarType;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.ivo.Money;
import org.tests.model.ivo.converter.MoneyTypeConverter;

import javax.persistence.EnumType;
import java.sql.SQLException;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class TestTypeManager extends BaseTestCase {

  @Test
  public void testEnumWithSubclasses() throws SQLException {

    DefaultTypeManager typeManager = createTypeManager();

    ScalarType<?> type = typeManager.createEnumScalarType(MyEnum.class, null);
    typeManager.addEnumType(type, MyEnum.class);

    Object val = type.read(new DummyDataReader("A"));
    assertThat(val).isEqualTo(MyEnum.Aval);
    val = type.read(new DummyDataReader("B"));
    assertThat(val).isEqualTo(MyEnum.Bval);
    val = type.read(new DummyDataReader("C"));
    assertThat(val).isEqualTo(MyEnum.Cval);

    ScalarType<?> typeGeneral = typeManager.getScalarType(MyEnum.class);
    assertThat(typeGeneral).isNotNull();
    ScalarType<?> typeB = typeManager.getScalarType(MyEnum.Bval.getClass());
    assertThat(typeB).isNotNull();
    ScalarType<?> typeA = typeManager.getScalarType(MyEnum.Aval.getClass());
    assertThat(typeA).isNotNull();
    ScalarType<?> typeC = typeManager.getScalarType(MyEnum.Cval.getClass());
    assertThat(typeC).isNotNull();

    try {
      typeManager.createEnumScalarType(MyEnum.class, EnumType.STRING);
      assertTrue("never get here",false);
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("It is mapped using 2 different modes when only one is supported");
    }
  }

  @Test
  public void testEnumWithChar() throws SQLException {

    DefaultTypeManager typeManager = createTypeManager();

    ScalarType<?> dayOfWeekType = typeManager.createEnumScalarType(MyDayOfWeek.class, null);

    Object val = dayOfWeekType.read(new DummyDataReader("MONDAY   "));
    assertThat(val).isEqualTo(MyDayOfWeek.MONDAY);

    val = dayOfWeekType.read(new DummyDataReader("TUESDAY  "));
    assertThat(val).isEqualTo(MyDayOfWeek.TUESDAY);

    val = dayOfWeekType.read(new DummyDataReader("WEDNESDAY"));
    assertThat(val).isEqualTo(MyDayOfWeek.WEDNESDAY);

    val = dayOfWeekType.read(new DummyDataReader("THURSDAY "));
    assertThat(val).isEqualTo(MyDayOfWeek.THURSDAY);

    val = dayOfWeekType.read(new DummyDataReader("FRIDAY   "));
    assertThat(val).isEqualTo(MyDayOfWeek.FRIDAY);

    try {
      typeManager.createEnumScalarType(MyDayOfWeek.class, EnumType.ORDINAL);
      assertTrue("never get here",false);
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("It is mapped using 2 different modes when only one is supported");
    }
  }

  @Test
  public void test() {

    DefaultTypeManager typeManager = createTypeManager();

    ScalarType<?> scalarType = typeManager.getScalarType(Money.class);
    assertTrue(scalarType.getJdbcType() == Types.DECIMAL);
    assertTrue(!scalarType.isJdbcNative());
    Assert.assertEquals(Money.class, scalarType.getType());

  }

  private DefaultTypeManager createTypeManager() {

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setDatabasePlatform(new H2Platform());

    BootupClasses bootupClasses = new BootupClasses();
    bootupClasses.getAttributeConverters().add(MoneyTypeConverter.class);

    return new DefaultTypeManager(serverConfig, bootupClasses);
  }

  /**
   * Test double DataReader implementation.
   */
  private static class DummyDataReader extends RsetDataReader {

    String val;

    DummyDataReader(String val) {
      super(null, null);
      this.val = val;
    }

    @Override
    public String getString() throws SQLException {
      return val;
    }
  }
}

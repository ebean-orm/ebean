package com.avaje.ebean.server.type;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.H2Platform;
import com.avaje.ebeaninternal.server.core.bootup.BootupClasses;
import com.avaje.ebeaninternal.server.type.CtCompoundType;
import com.avaje.ebeaninternal.server.type.DefaultTypeManager;
import com.avaje.ebeaninternal.server.type.RsetDataReader;
import com.avaje.ebeaninternal.server.type.ScalarDataReader;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.type.reflect.CheckImmutableResponse;
import com.avaje.tests.model.ivo.CMoney;
import com.avaje.tests.model.ivo.ExhangeCMoneyRate;
import com.avaje.tests.model.ivo.Money;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTypeManager extends BaseTestCase {

  @Test
  public void testEnumWithSubclasses() throws SQLException {

    DefaultTypeManager typeManager = createTypeManager();

    ScalarType<?> type = typeManager.createEnumScalarType(MyEnum.class);
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
  }

  @Test
  public void testEnumWithChar() throws SQLException {

    DefaultTypeManager typeManager = createTypeManager();

    ScalarType<?> dayOfWeekType = typeManager.createEnumScalarType(MyDayOfWeek.class);

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
  }

  @Test
  public void test() {

    DefaultTypeManager typeManager = createTypeManager();

    CheckImmutableResponse checkImmutable = typeManager.checkImmutable(Money.class);
    Assert.assertTrue(checkImmutable.isImmutable());

    checkImmutable = typeManager.checkImmutable(CMoney.class);
    Assert.assertTrue(checkImmutable.isImmutable());

    ScalarDataReader<?> dataReader = typeManager
      .recursiveCreateScalarDataReader(ExhangeCMoneyRate.class);
    Assert.assertTrue(dataReader instanceof CtCompoundType<?>);

    dataReader = typeManager.recursiveCreateScalarDataReader(CMoney.class);
    Assert.assertTrue(dataReader instanceof CtCompoundType<?>);

    ScalarType<?> scalarType = typeManager.recursiveCreateScalarTypes(Money.class);
    Assert.assertTrue(scalarType.getJdbcType() == Types.DECIMAL);
    Assert.assertTrue(!scalarType.isJdbcNative());
    Assert.assertEquals(Money.class, scalarType.getType());

  }

  private DefaultTypeManager createTypeManager() {

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setDatabasePlatform(new H2Platform());

    BootupClasses bootupClasses = new BootupClasses();

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

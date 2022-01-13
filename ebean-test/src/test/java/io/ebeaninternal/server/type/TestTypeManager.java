package io.ebeaninternal.server.type;

import io.ebean.BaseTestCase;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.core.type.ScalarType;
import io.ebean.server.type.MyDayOfWeek;
import io.ebean.server.type.MyEnum;
import io.ebean.server.type.MySex;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import org.junit.jupiter.api.Test;
import org.tests.model.ivo.Money;
import org.tests.model.ivo.converter.MoneyTypeConverter;

import javax.persistence.EnumType;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestTypeManager extends BaseTestCase {

  @Test
  public void testEnumWithSubclasses() throws SQLException {

    DefaultTypeManager typeManager = createTypeManager();

    ScalarType<?> type = typeManager.createEnumScalarType(MyEnum.class, null);

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
      fail("never get here");
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
      fail("never get here");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("It is mapped using 2 different modes when only one is supported");
    }
  }

  @Test
  public void test() {
    DefaultTypeManager typeManager = createTypeManager();

    ScalarType<?> scalarType = typeManager.getScalarType(Money.class);
    assertEquals(Types.DECIMAL, scalarType.getJdbcType());
    assertFalse(scalarType.isJdbcNative());
    assertEquals(Money.class, scalarType.getType());
  }

  @Test
  public void testWithConfig() {
    DefaultTypeManager typeManager1 = createTypeManager();
    ScalarType<?> type1 = typeManager1.createEnumScalarType(MySex.class, null);
    assertThat(type1).isInstanceOf(ScalarTypeEnumStandard.OrdinalEnum.class);
    //
    DefaultTypeManager typeManager2 = createTypeManagerDefaultEnumTypeString();
    ScalarType<?> type2 = typeManager2.createEnumScalarType(MySex.class, null);
    assertThat(type2).isInstanceOf(ScalarTypeEnumStandard.StringEnum.class);
    //
    DefaultTypeManager typeManager3 = createTypeManagerDefaultEnumTypeString();
    ScalarType<?> type3 = typeManager3.createEnumScalarType(MySex.class, EnumType.ORDINAL);
    assertThat(type3).isInstanceOf(ScalarTypeEnumStandard.OrdinalEnum.class);
  }

  private DefaultTypeManager createTypeManager() {
    DatabaseConfig config = new DatabaseConfig();
    config.setDatabasePlatform(new H2Platform());

    BootupClasses bootupClasses = new BootupClasses();
    bootupClasses.getAttributeConverters().add(MoneyTypeConverter.class);

    return new DefaultTypeManager(config, bootupClasses);
  }

  private DefaultTypeManager createTypeManagerDefaultEnumTypeString() {
    DatabaseConfig config = new DatabaseConfig();
    config.setDatabasePlatform(new H2Platform());
    config.setDefaultEnumType(EnumType.STRING);

    BootupClasses bootupClasses = new BootupClasses();
    bootupClasses.getAttributeConverters().add(MoneyTypeConverter.class);

    return new DefaultTypeManager(config, bootupClasses);
  }

  @Test
  public void testCalendar() throws SQLException {

    DefaultTypeManager typeManager = createTypeManager();
    ScalarType<?> typeB = typeManager.getScalarType(GregorianCalendar.class);
    assertThat(typeB).isInstanceOf(ScalarTypeCalendar.class);
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

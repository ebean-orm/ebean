package io.ebeaninternal.server.type;

import io.ebean.DatabaseBuilder;
import io.ebean.config.DatabaseConfig;
import io.ebean.platform.h2.H2Platform;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.BaseTest;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.enums.MyDayOfWeek;
import org.tests.model.basic.enums.MyEnum;
import org.tests.model.basic.enums.MySex;
import org.tests.model.ivo.Money;
import org.tests.model.ivo.converter.MoneyTypeConverter;

import jakarta.persistence.EnumType;
import java.sql.SQLException;
import java.sql.Types;
import java.util.GregorianCalendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestTypeManager extends BaseTest {

  @Test
  void testEnumWithSubclasses() throws SQLException {
    DefaultTypeManager typeManager = createTypeManager();

    ScalarType<?> type = typeManager.enumType(MyEnum.class, null);

    DataReader reader = mock(DataReader.class);
    when(reader.getString()).thenReturn("A");
    Object val = type.read(reader);
    assertThat(val).isEqualTo(MyEnum.Aval);
    when(reader.getString()).thenReturn("B");
    val = type.read(reader);
    assertThat(val).isEqualTo(MyEnum.Bval);
    when(reader.getString()).thenReturn("C");
    val = type.read(reader);
    assertThat(val).isEqualTo(MyEnum.Cval);

    ScalarType<?> typeGeneral = typeManager.type(MyEnum.class);
    assertThat(typeGeneral).isNotNull();
    ScalarType<?> typeB = typeManager.type(MyEnum.Bval.getClass());
    assertThat(typeB).isNotNull();
    ScalarType<?> typeA = typeManager.type(MyEnum.Aval.getClass());
    assertThat(typeA).isNotNull();
    ScalarType<?> typeC = typeManager.type(MyEnum.Cval.getClass());
    assertThat(typeC).isNotNull();

    try {
      typeManager.enumType(MyEnum.class, EnumType.STRING);
      fail("never get here");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("It is mapped using 2 of (ORDINAL, STRING or an Ebean mapping) when only one is supported.");
    }
  }

  @Test
  void testEnumWithChar() throws SQLException {
    DefaultTypeManager typeManager = createTypeManager();

    ScalarType<?> dayOfWeekType = typeManager.enumType(MyDayOfWeek.class, null);
    DataReader reader = mock(DataReader.class);
    when(reader.getString()).thenReturn("MONDAY   ");
    Object val = dayOfWeekType.read(reader);
    assertThat(val).isEqualTo(MyDayOfWeek.MONDAY);

    when(reader.getString()).thenReturn("TUESDAY  ");
    val = dayOfWeekType.read(reader);
    assertThat(val).isEqualTo(MyDayOfWeek.TUESDAY);

    when(reader.getString()).thenReturn("WEDNESDAY");
    val = dayOfWeekType.read(reader);
    assertThat(val).isEqualTo(MyDayOfWeek.WEDNESDAY);

    when(reader.getString()).thenReturn("THURSDAY ");
    val = dayOfWeekType.read(reader);
    assertThat(val).isEqualTo(MyDayOfWeek.THURSDAY);

    when(reader.getString()).thenReturn("FRIDAY   ");
    val = dayOfWeekType.read(reader);
    assertThat(val).isEqualTo(MyDayOfWeek.FRIDAY);

    try {
      typeManager.enumType(MyDayOfWeek.class, EnumType.ORDINAL);
      fail("never get here");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("It is mapped using 2 of (ORDINAL, STRING or an Ebean mapping) when only one is supported.");
    }
  }

  @Test
  void test() {
    DefaultTypeManager typeManager = createTypeManager();

    ScalarType<?> scalarType = typeManager.type(Money.class);
    assertEquals(Types.DECIMAL, scalarType.jdbcType());
    assertFalse(scalarType.jdbcNative());
    assertEquals(Money.class, scalarType.type());
  }

  @Test
  void testWithConfig() {
    DefaultTypeManager typeManager1 = createTypeManager();
    ScalarType<?> type1 = typeManager1.enumType(MySex.class, null);
    assertThat(type1).isInstanceOf(ScalarTypeEnumStandard.OrdinalEnum.class);
    //
    DefaultTypeManager typeManager2 = createTypeManagerDefaultEnumTypeString();
    ScalarType<?> type2 = typeManager2.enumType(MySex.class, null);
    assertThat(type2).isInstanceOf(ScalarTypeEnumStandard.StringEnum.class);
    //
    DefaultTypeManager typeManager3 = createTypeManagerDefaultEnumTypeString();
    ScalarType<?> type3 = typeManager3.enumType(MySex.class, EnumType.ORDINAL);
    assertThat(type3).isInstanceOf(ScalarTypeEnumStandard.OrdinalEnum.class);
  }

  private DefaultTypeManager createTypeManager() {
    DatabaseBuilder config = new DatabaseConfig();
    config.setDatabasePlatform(new H2Platform());

    BootupClasses bootupClasses = new BootupClasses();
    bootupClasses.getAttributeConverters().add(MoneyTypeConverter.class);

    return new DefaultTypeManager(config, bootupClasses);
  }

  private DefaultTypeManager createTypeManagerDefaultEnumTypeString() {
    DatabaseBuilder config = new DatabaseConfig();
    config.setDatabasePlatform(new H2Platform());
    config.setDefaultEnumType(EnumType.STRING);

    BootupClasses bootupClasses = new BootupClasses();
    bootupClasses.getAttributeConverters().add(MoneyTypeConverter.class);

    return new DefaultTypeManager(config, bootupClasses);
  }

  @Test
  void testCalendar() {
    DefaultTypeManager typeManager = createTypeManager();
    ScalarType<?> typeB = typeManager.type(GregorianCalendar.class);
    assertThat(typeB).isInstanceOf(ScalarTypeCalendar.class);
  }

}

package io.ebeaninternal.server.type;

import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import org.junit.jupiter.api.Test;
import org.tests.model.array.IntEnum;
import org.tests.model.array.VarcharEnum;
import org.tests.model.basic.Car;

import javax.persistence.EnumType;
import java.time.DayOfWeek;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultTypeManagerTest {

  private DefaultTypeManager create() {
    DatabaseConfig serverConfig = new DatabaseConfig();
    serverConfig.setDatabasePlatform(new PostgresPlatform());
    BootupClasses bootupClasses = new BootupClasses();
    return new DefaultTypeManager(serverConfig, bootupClasses);
  }

  @Test
  public void isIntegerType() {
    DefaultTypeManager typeManager = create();

    assertTrue(typeManager.isIntegerType("1"));
    assertTrue(typeManager.isIntegerType("0"));

    assertFalse(typeManager.isIntegerType("A"));
    assertFalse(typeManager.isIntegerType("01"));
    assertFalse(typeManager.isIntegerType(" 01"));
    assertFalse(typeManager.isIntegerType(" 0"));
    assertFalse(typeManager.isIntegerType(" 1"));
    assertFalse(typeManager.isIntegerType(" A"));
  }


  @Test
  public void enumDayMonth_builtIn_overrideAsString() {
    DefaultTypeManager typeManager = create();

    ScalarType<?> type = typeManager.createEnumScalarType(Month.class, null);
    assertThat(type).isInstanceOf(ScalarTypeEnumWithMapping.class).as("built in type");

    // mapped explicitly as JPA EnumType.STRING
    type = typeManager.createEnumScalarType(Month.class, EnumType.STRING);
    assertThat(type).isInstanceOf(ScalarTypeEnumStandard.StringEnum.class).as("override built in type");
    try {
      typeManager.createEnumScalarType(Month.class, EnumType.ORDINAL);
      assertThat(true).isFalse().as("never get here");

    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("It is mapped using 2 different modes when only one is supported");
    }
  }

  @Test
  public void enumMonth_builtIn_overrideAsOrdinal() {
    DefaultTypeManager typeManager = create();

    // mapped explicitly as JPA EnumType.STRING
    ScalarType<?> type = typeManager.createEnumScalarType(Month.class, EnumType.ORDINAL);
    assertThat(type).isInstanceOf(ScalarTypeEnumStandard.OrdinalEnum.class).as("override built in type");
    try {
      typeManager.createEnumScalarType(Month.class, EnumType.STRING);
      assertThat(true).isFalse().as("never get here");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("It is mapped using 2 different modes when only one is supported");
    }
  }

  @Test
  public void enumDayOfWeek_builtIn_overrideAsString() {
    DefaultTypeManager typeManager = create();

    ScalarType<?> type = typeManager.createEnumScalarType(DayOfWeek.class, null);
    assertThat(type).isInstanceOf(ScalarTypeEnumWithMapping.class).as("built in type");

    // mapped explicitly as JPA EnumType.STRING
    type = typeManager.createEnumScalarType(DayOfWeek.class, EnumType.STRING);
    assertThat(type).isInstanceOf(ScalarTypeEnumStandard.StringEnum.class).as("override built in type");
    try {
      typeManager.createEnumScalarType(DayOfWeek.class, EnumType.ORDINAL);
      assertThat(true).isFalse().as("never get here");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("It is mapped using 2 different modes when only one is supported");
    }
  }

  @Test
  public void enumDayOfWeek_builtIn_overrideAsOrdinal() {
    DefaultTypeManager typeManager = create();

    // mapped explicitly as JPA EnumType.STRING
    ScalarType<?> type = typeManager.createEnumScalarType(DayOfWeek.class, EnumType.ORDINAL);
    assertThat(type).isInstanceOf(ScalarTypeEnumStandard.OrdinalEnum.class).as("override built in type");
    try {
      typeManager.createEnumScalarType(DayOfWeek.class, EnumType.STRING);
      assertThat(true).isFalse().as("never get here");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).contains("It is mapped using 2 different modes when only one is supported");
    }
  }

  @Test
  public void createEnumScalarTypePerExtentions() {
    DefaultTypeManager typeManager = create();

    ScalarType<?> type = typeManager.createEnumScalarType(VarcharEnum.class, EnumType.ORDINAL);
    assertThat(type).isInstanceOf(ScalarTypeEnumWithMapping.class);
    // withConstraint false
    assertThat(((ScalarTypeEnumWithMapping) type).getDbCheckConstraintValues()).isNull();

    type = typeManager.createEnumScalarType(IntEnum.class, EnumType.ORDINAL);
    assertThat(type).isInstanceOf(ScalarTypeEnumWithMapping.class);
    ScalarTypeEnumWithMapping enumWithMapping = (ScalarTypeEnumWithMapping) type;
    // withConstraint true
    assertThat(enumWithMapping.getDbCheckConstraintValues()).hasSize(3);
    assertThat(enumWithMapping.getDbCheckConstraintValues()).contains("100", "101", "102");

    type = typeManager.createEnumScalarType(Car.Size.class, EnumType.ORDINAL);
    assertThat(type).isInstanceOf(ScalarTypeEnumWithMapping.class);
    enumWithMapping = (ScalarTypeEnumWithMapping) type;
    // withConstraint true
    assertThat(enumWithMapping.getDbCheckConstraintValues()).hasSize(2);
    assertThat(enumWithMapping.getDbCheckConstraintValues()).contains("'L'", "'S'");
  }
}

package io.ebean.server.type;

import io.ebean.annotation.EnumValue;

/**
 * Enum test when DB CHAR column used with spaces.
 */
public enum MyDayOfWeek {

  @EnumValue("MONDAY   ")MONDAY,
  @EnumValue("TUESDAY  ")TUESDAY,
  @EnumValue("WEDNESDAY")WEDNESDAY,
  @EnumValue("THURSDAY ")THURSDAY,
  @EnumValue("FRIDAY   ")FRIDAY,
  @EnumValue("SATURDAY ")SATURDAY,
  @EnumValue("SUNDAY   ")SUNDAY
}

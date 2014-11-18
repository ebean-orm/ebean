package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.CompoundType;
import com.avaje.ebean.config.CompoundTypeProperty;

import java.time.Period;

/**
 * Compound type for Period value type.
 * <p>
 * Persists the Period into 3 separate integer columns for years, months and days.
 * </p>
 */
public class CompoundTypePeriod implements CompoundType<Period> {

  CompoundTypeProperty[] properties = new CompoundTypeProperty[3];

  public CompoundTypePeriod() {
    properties[0] = new CTPeriodYear();
    properties[1] = new CTPeriodMonth();
    properties[2] = new CTPeriodDay();
  }

  @Override
  public Period create(Object[] propertyValues) {
    return Period.of((Integer)propertyValues[0], (Integer)propertyValues[1], (Integer)propertyValues[2]);
  }

  @Override
  public CompoundTypeProperty<Period, ?>[] getProperties() {
    return properties;
  }

  static class CTPeriodYear implements CompoundTypeProperty<Period, Integer> {

    public String getName() {
      return "years";
    }

    public Integer getValue(Period valueObject) {
      return valueObject.getYears();
    }

    public int getDbType() {
      return 0;
    }

  }

  static class CTPeriodMonth implements CompoundTypeProperty<Period, Integer> {

    public String getName() {
      return "months";
    }

    public Integer getValue(Period valueObject) {
      return valueObject.getMonths();
    }

    public int getDbType() {
      return 0;
    }

  }

  static class CTPeriodDay implements CompoundTypeProperty<Period, Integer> {

    public String getName() {
      return "days";
    }

    public Integer getValue(Period valueObject) {
      return valueObject.getDays();
    }

    public int getDbType() {
      return 0;
    }

  }
}

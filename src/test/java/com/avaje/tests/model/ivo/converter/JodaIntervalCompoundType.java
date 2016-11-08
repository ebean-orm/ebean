package com.avaje.tests.model.ivo.converter;

import com.avaje.ebean.config.CompoundType;
import com.avaje.ebean.config.CompoundTypeProperty;
import org.joda.time.Interval;

public class JodaIntervalCompoundType implements CompoundType<Interval> {

  public Interval create(Object[] propertyValues) {
    return new Interval((Long) propertyValues[0], (Long) propertyValues[1]);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public CompoundTypeProperty<Interval, ?>[] getProperties() {
    return new CompoundTypeProperty[]{new Start(), new End()};
  }

  static class Start implements CompoundTypeProperty<Interval, Long> {

    public String getName() {
      return "startMillis";
    }

    public Long getValue(Interval valueObject) {
      return valueObject.getStartMillis();
    }

    public int getDbType() {
      return java.sql.Types.TIMESTAMP;
    }
  }

  static class End implements CompoundTypeProperty<Interval, Long> {

    public String getName() {
      return "endMillis";
    }

    public Long getValue(Interval valueObject) {
      return valueObject.getEndMillis();
    }

    public int getDbType() {
      return java.sql.Types.TIMESTAMP;
    }

  }

}

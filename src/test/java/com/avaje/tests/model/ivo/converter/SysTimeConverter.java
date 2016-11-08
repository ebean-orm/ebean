package com.avaje.tests.model.ivo.converter;

import com.avaje.ebean.config.ScalarTypeConverter;
import com.avaje.tests.model.ivo.SysTime;

import java.sql.Timestamp;

public class SysTimeConverter implements ScalarTypeConverter<SysTime, Timestamp> {


  public SysTime getNullValue() {
    return null;
  }

  public Timestamp unwrapValue(SysTime beanType) {
    return new Timestamp(beanType.getMillis());
  }

  public SysTime wrapValue(Timestamp scalarType) {
    return new SysTime(scalarType.getTime());
  }


}

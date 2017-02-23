package org.tests.model.ivo.converter;

import io.ebean.config.ScalarTypeConverter;
import org.tests.model.ivo.SysTime;

import java.sql.Timestamp;

public class SysTimeConverter implements ScalarTypeConverter<SysTime, Timestamp> {


  @Override
  public SysTime getNullValue() {
    return null;
  }

  @Override
  public Timestamp unwrapValue(SysTime beanType) {
    return new Timestamp(beanType.getMillis());
  }

  @Override
  public SysTime wrapValue(Timestamp scalarType) {
    return new SysTime(scalarType.getTime());
  }


}

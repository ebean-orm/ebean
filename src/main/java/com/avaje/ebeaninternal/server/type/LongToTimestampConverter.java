package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.ScalarTypeConverter;

import java.sql.Timestamp;

public class LongToTimestampConverter implements ScalarTypeConverter<Long, Timestamp> {

  public Long getNullValue() {
    return null;
  }

  public Timestamp unwrapValue(Long beanType) {

    return new Timestamp(beanType);
  }

  public Long wrapValue(Timestamp scalarType) {

    return scalarType.getTime();
  }


}

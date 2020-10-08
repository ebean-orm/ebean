package io.ebeaninternal.server.type;

import io.ebean.config.ScalarTypeConverter;

import java.sql.Timestamp;

public class LongToTimestampConverter implements ScalarTypeConverter<Long, Timestamp> {

  @Override
  public Long getNullValue() {
    return null;
  }

  @Override
  public Timestamp unwrapValue(Long beanType) {

    return new Timestamp(beanType);
  }

  @Override
  public Long wrapValue(Timestamp scalarType) {

    return scalarType.getTime();
  }


}

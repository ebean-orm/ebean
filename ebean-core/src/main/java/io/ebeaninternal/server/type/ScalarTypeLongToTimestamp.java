package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;

import java.sql.Timestamp;

public class ScalarTypeLongToTimestamp extends ScalarTypeWrapper<Long, Timestamp> {

  public ScalarTypeLongToTimestamp(JsonConfig.DateTime mode) {
    super(Long.class, new ScalarTypeTimestamp(mode), new LongToTimestampConverter());
  }
}

package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.JsonConfig;

import java.sql.Timestamp;

public class ScalarTypeLongToTimestamp extends ScalarTypeWrapper<Long, Timestamp> {

    public ScalarTypeLongToTimestamp(JsonConfig.DateTime mode) {
        super(Long.class, new ScalarTypeTimestamp(mode), new LongToTimestampConverter());
    }
}

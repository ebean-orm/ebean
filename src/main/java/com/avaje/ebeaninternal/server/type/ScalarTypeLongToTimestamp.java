package com.avaje.ebeaninternal.server.type;

import java.sql.Timestamp;

public class ScalarTypeLongToTimestamp extends ScalarTypeWrapper<Long, Timestamp> {

    public ScalarTypeLongToTimestamp() {
        super(Long.class, new ScalarTypeTimestamp(), new LongToTimestampConverter());
    }
}

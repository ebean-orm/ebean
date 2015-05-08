package com.avaje.ebeaninternal.server.type;

import java.sql.Timestamp;

import com.avaje.ebean.config.ScalarTypeConverter;

public class LongToTimestampConverter implements ScalarTypeConverter<Long, Timestamp>{
    
    public Long getNullValue() {
        return null;
    }

    public Timestamp unwrapValue(Long beanType) {
        
        return new Timestamp(beanType.longValue());
    }

    public Long wrapValue(Timestamp scalarType) {
        
        return scalarType.getTime();
    }

    
}

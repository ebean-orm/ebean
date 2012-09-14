package com.avaje.ebeaninternal.server.type;

import scala.Double;

import com.avaje.ebean.config.ScalarTypeConverter;

public class ScalarTypeScalaDouble extends ScalarTypeWrapper<Object, java.lang.Double> {

    public ScalarTypeScalaDouble() {
        super(Object.class, new ScalarTypeDouble(), new Converter());
    }
    
    static class Converter implements ScalarTypeConverter<Object, java.lang.Double> {

      public Double getNullValue() {
        return null;
      }

      public Object wrapValue(java.lang.Double scalarType) {
        return scalarType;
      }

      public java.lang.Double unwrapValue(Object beanType) {
        return ((scala.Double)beanType).toDouble();
      }
      
    }
}

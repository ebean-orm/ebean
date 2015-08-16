package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

import org.joda.time.LocalDateTime;
import org.joda.time.DateTime;

/**
 * Support java.time DateTime types as GeneratedProperty.
 */
public class GeneratedUpdateJodaTime {

  public static abstract class Base implements GeneratedProperty, GeneratedWhenModified {

    @Override
    public boolean includeInUpdate() {
      return true;
    }

    @Override
    public boolean includeInAllUpdates() {
      return true;
    }

    @Override
    public boolean includeInInsert() {
      return true;
    }

    @Override
    public boolean isDDLNotNullable() {
      return true;
    }
  }

  /**
   * LocalDateTime support.
   */
  public static class LocalDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean) {
      return new LocalDateTime();
    }

    @Override
    public Object getUpdateValue(BeanProperty prop, EntityBean bean) {
      return new LocalDateTime();
    }
  }

  /**
   * OffsetDateTime support.
   */
  public static class DateTimeDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean) {
      return new DateTime();
    }

    @Override
    public Object getUpdateValue(BeanProperty prop, EntityBean bean) {
      return new DateTime();
    }
  }

}

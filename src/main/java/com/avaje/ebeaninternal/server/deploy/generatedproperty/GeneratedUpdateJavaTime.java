package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

/**
 * Support java.time DateTime types as GeneratedProperty.
 */
public class GeneratedUpdateJavaTime {

  public static abstract class Base implements GeneratedProperty {

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
      return LocalDateTime.now();
    }

    @Override
    public Object getUpdateValue(BeanProperty prop, EntityBean bean) {
      return LocalDateTime.now();
    }
  }

  /**
   * OffsetDateTime support.
   */
  public static class OffsetDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean) {
      return OffsetDateTime.now();
    }

    @Override
    public Object getUpdateValue(BeanProperty prop, EntityBean bean) {
      return OffsetDateTime.now();
    }
  }

  /**
   * ZonedDateTime support.
   */
  public static class ZonedDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean) {
      return ZonedDateTime.now();
    }

    @Override
    public Object getUpdateValue(BeanProperty prop, EntityBean bean) {
      return ZonedDateTime.now();
    }
  }

}

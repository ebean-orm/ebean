package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

/**
 * Support java.time types as GeneratedProperty.
 */
public class GeneratedInsertJavaTime {

  public static abstract class Base implements GeneratedProperty {

    @Override
    public boolean includeInUpdate() {
      return false;
    }

    @Override
    public boolean includeInAllUpdates() {
      return false;
    }

    @Override
    public boolean includeInInsert() {
      return true;
    }

    @Override
    public boolean isDDLNotNullable() {
      return true;
    }

    @Override
    public Object getUpdateValue(BeanProperty prop, EntityBean bean) {
      return prop.getValue(bean);
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
  }

  /**
   * OffsetDateTime support.
   */
  public static class OffsetDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean) {
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

  }

}

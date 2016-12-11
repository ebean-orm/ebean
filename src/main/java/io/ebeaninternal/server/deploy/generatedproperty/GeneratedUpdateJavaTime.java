package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;

/**
 * Support java.time DateTime types as GeneratedProperty.
 */
public class GeneratedUpdateJavaTime {

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
   * Instant support.
   */
  public static class InstantDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
      return JavaTimeUtils.toInstant(now);
    }

    @Override
    public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
      return JavaTimeUtils.toInstant(now);
    }
  }

  /**
   * LocalDateTime support.
   */
  public static class LocalDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
      return JavaTimeUtils.toLocalDateTime(now);
    }

    @Override
    public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
      return JavaTimeUtils.toLocalDateTime(now);
    }
  }

  /**
   * OffsetDateTime support.
   */
  public static class OffsetDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
      return JavaTimeUtils.toOffsetDateTime(now);
    }

    @Override
    public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
      return JavaTimeUtils.toOffsetDateTime(now);
    }
  }

  /**
   * ZonedDateTime support.
   */
  public static class ZonedDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
      return JavaTimeUtils.toZonedDateTime(now);
    }

    @Override
    public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
      return JavaTimeUtils.toZonedDateTime(now);
    }
  }

}

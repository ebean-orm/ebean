package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;

/**
 * Support java.time types as GeneratedProperty.
 */
final class GeneratedInsertJavaTime {

  static abstract class Base implements GeneratedProperty, GeneratedWhenCreated {

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
    public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
      return prop.getValue(bean);
    }
  }

  /**
   * Instant support.
   */
  static final class InstantDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
      return JavaTimeUtils.toInstant(now);
    }
  }

  /**
   * LocalDateTime support.
   */
  static final class LocalDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
      return JavaTimeUtils.toLocalDateTime(now);
    }
  }

  /**
   * OffsetDateTime support.
   */
  static final class OffsetDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
      return JavaTimeUtils.toOffsetDateTime(now);
    }

  }

  /**
   * ZonedDateTime support.
   */
  static final class ZonedDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
      return JavaTimeUtils.toZonedDateTime(now);
    }

  }

}

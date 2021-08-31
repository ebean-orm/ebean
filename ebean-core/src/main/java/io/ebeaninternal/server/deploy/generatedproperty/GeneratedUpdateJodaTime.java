package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

/**
 * Support java.time DateTime types as GeneratedProperty.
 */
final class GeneratedUpdateJodaTime {

  static abstract class Base implements GeneratedProperty, GeneratedWhenModified {

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
  static final class LocalDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
      return new LocalDateTime(now);
    }

    @Override
    public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
      return new LocalDateTime(now);
    }
  }

  /**
   * OffsetDateTime support.
   */
  static final class DateTimeDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
      return new DateTime(now);
    }

    @Override
    public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
      return new DateTime(now);
    }
  }

}

package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

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
  public static class DateTimeDT extends Base {

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

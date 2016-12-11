package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

/**
 * Support joda time types as GeneratedProperty.
 */
public class GeneratedInsertJodaTime {

  public static abstract class Base implements GeneratedProperty, GeneratedWhenCreated {

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
   * LocalDateTime support.
   */
  public static class LocalDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
      return new LocalDateTime(now);
    }
  }

  /**
   * DateTime support.
   */
  public static class DateTimeDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
      return new DateTime(now);
    }

  }


}

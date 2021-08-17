package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

/**
 * Support joda time types as GeneratedProperty.
 */
final class GeneratedInsertJodaTime {

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
   * LocalDateTime support.
   */
  static final class LocalDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
      return new LocalDateTime(now);
    }
  }

  /**
   * DateTime support.
   */
  static final class DateTimeDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
      return new DateTime(now);
    }

  }


}

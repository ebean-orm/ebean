package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

import org.joda.time.LocalDateTime;
import org.joda.time.DateTime;

/**
 * Support joda time types as GeneratedProperty.
 */
public class GeneratedInsertJodaTime {

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
      return new LocalDateTime();
    }
  }

  /**
   * DateTime support.
   */
  public static class DateTimeDT extends Base {

    @Override
    public Object getInsertValue(BeanProperty prop, EntityBean bean) {
      return new DateTime();
    }

  }


}

package com.avaje.ebeaninternal.server.properties;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import javax.persistence.PersistenceException;

import com.avaje.ebean.bean.EntityBean;

/**
 * A BeanReflect implementation based on the enhancement that creates EntityBean
 * implementations.
 */
public final class EnhanceBeanPropertyInfo implements BeanPropertyInfo {

  private static final Object[] constuctorArgs = new Object[0];

  private final Constructor<?> constructor;

  public EnhanceBeanPropertyInfo(Class<?> clazz) {
    try {
      if (Modifier.isAbstract(clazz.getModifiers())) {
        this.constructor = null;  
      } else {
        this.constructor = defaultConstructor(clazz);
      }
      
    } catch (Exception e) {
      throw new PersistenceException(e);
    }
  }

  private Constructor<?> defaultConstructor(Class<?> cls) {
    try {
      Class<?>[] params = new Class[0];
      return cls.getDeclaredConstructor(params);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public Object createEntityBean() {
    try {
      return constructor.newInstance(constuctorArgs);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public BeanPropertyGetter getGetter(String name, int position) {
    return new Getter(position);
  }

  public BeanPropertySetter getSetter(String name, int position) {
    return new Setter(position);
  }

  static final class Getter implements BeanPropertyGetter {

    private final int fieldIndex;

    Getter(int fieldIndex) {
      this.fieldIndex = fieldIndex;
    }

    public Object get(EntityBean bean) {
      return bean._ebean_getField(fieldIndex);
    }

    public Object getIntercept(EntityBean bean) {
      return bean._ebean_getFieldIntercept(fieldIndex);
    }
  }

  static final class Setter implements BeanPropertySetter {

    private final int fieldIndex;

    Setter(int fieldIndex) {
      this.fieldIndex = fieldIndex;
    }

    public void set(EntityBean bean, Object value) {
      bean._ebean_setField(fieldIndex, value);
    }

    public void setIntercept(EntityBean bean, Object value) {
      bean._ebean_setFieldIntercept(fieldIndex, value);
    }

  }
}

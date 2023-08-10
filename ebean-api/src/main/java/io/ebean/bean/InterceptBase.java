package io.ebean.bean;

/**
 * Base class for InterceptReadOnly / InterceptReadWrite. This class should contain only the essential member variables to keep
 * the memory footprint low.
 *
 * @author Roland Praml, FOCONIS AG
 */
public abstract class InterceptBase implements EntityBeanIntercept {

  /**
   * The actual entity bean that 'owns' this intercept.
   */
  protected final EntityBean owner;

  protected InterceptBase(EntityBean owner) {
    this.owner = owner;
  }

  protected ExtensionAccessor findAccessor(int index) {
    return owner._ebean_getExtensionAccessors().findAccessor(index);
  }

  private int getOffset(ExtensionAccessor accessor) {
    return owner._ebean_getExtensionAccessors().getOffset(accessor);
  }

  protected EntityBean getExtensionBean(ExtensionAccessor accessor) {
    return owner._ebean_getExtension(accessor);
  }

  @Override
  public int findProperty(String propertyName) {
    String[] names = owner._ebean_getPropertyNames();
    int i;
    for (i = 0; i < names.length; i++) {
      if (names[i].equals(propertyName)) {
        return i;
      }
    }
    for (ExtensionAccessor acc : owner._ebean_getExtensionAccessors()) {
      names = acc.getProperties();
      for (int j = 0; j < names.length; j++) {
        if (names[j].equals(propertyName)) {
          return i;
        }
        i++;
      }
    }
    return -1;
  }

  @Override
  public String property(int propertyIndex) {
    if (propertyIndex == -1) {
      return null;
    }
    ExtensionAccessor accessor = findAccessor(propertyIndex);
    if (accessor == null) {
      return owner._ebean_getPropertyName(propertyIndex);
    } else {
      int offset = getOffset(accessor);
      return getExtensionBean(accessor)._ebean_getPropertyName(propertyIndex - offset);
    }
  }

  @Override
  public int propertyLength() {
    return owner._ebean_getPropertyNames().length
      + owner._ebean_getExtensionAccessors().getPropertyLength();
  }

  @Override
  public Object value(int index) {
    ExtensionAccessor accessor = findAccessor(index);
    if (accessor == null) {
      return owner._ebean_getField(index);
    } else {
      int offset = getOffset(accessor);
      return getExtensionBean(accessor)._ebean_getField(index - offset);
    }
  }

  @Override
  public Object valueIntercept(int index) {
    ExtensionAccessor accessor = findAccessor(index);
    if (accessor == null) {
      return owner._ebean_getFieldIntercept(index);
    } else {
      int offset = getOffset(accessor);
      return getExtensionBean(accessor)._ebean_getFieldIntercept(index - offset);
    }
  }

  @Override
  public void setValue(int index, Object value) {
    ExtensionAccessor accessor = findAccessor(index);
    if (accessor == null) {
      owner._ebean_setField(index, value);
    } else {
      int offset = getOffset(accessor);
      getExtensionBean(accessor)._ebean_setField(index - offset, value);
    }
  }

  @Override
  public void setValueIntercept(int index, Object value) {
    ExtensionAccessor accessor = findAccessor(index);
    if (accessor == null) {
      owner._ebean_setFieldIntercept(index, value);
    } else {
      int offset = getOffset(accessor);
      getExtensionBean(accessor)._ebean_setFieldIntercept(index - offset, value);
    }
  }
}

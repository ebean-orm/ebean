package io.ebean.bean;

/**
 * Common base features for EntityBeanIntercept.
 */
abstract class InterceptBase implements EntityBeanIntercept {

  final EntityBean owner;
  boolean fullyLoadedBean;
  boolean errorOnLazyLoad;

  /**
   * Create with a given entity.
   */
  InterceptBase(Object ownerBean) {
    this.owner = (EntityBean) ownerBean;
  }

  /**
   * EXPERIMENTAL - Constructor only for use by serialization frameworks.
   */
  InterceptBase() {
    this.owner = null;
  }

  @Override
  public final EntityBean owner() {
    return owner;
  }

  @Override
  public final void errorOnLazyLoad(boolean errorOnLazyLoad) {
    this.errorOnLazyLoad = errorOnLazyLoad;
  }

  @Override
  public final boolean isFullyLoadedBean() {
    return fullyLoadedBean;
  }

  @Override
  public final void setFullyLoadedBean(boolean fullyLoadedBean) {
    this.fullyLoadedBean = fullyLoadedBean;
  }

  @Override
  public final String property(int propertyIndex) {
    if (propertyIndex == -1) {
      return null;
    }
    return owner._ebean_getPropertyName(propertyIndex);
  }

  @Override
  public final int findProperty(String propertyName) {
    final String[] names = owner._ebean_getPropertyNames();
    for (int i = 0; i < names.length; i++) {
      if (names[i].equals(propertyName)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public final StringBuilder loadedPropertyKey() {
    final StringBuilder sb = new StringBuilder();
    final int len = propertyLength();
    for (int i = 0; i < len; i++) {
      if (isLoadedProperty(i)) {
        sb.append(i).append(',');
      }
    }
    return sb;
  }
}

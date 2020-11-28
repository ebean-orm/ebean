package io.ebeaninternal.server.core;

import io.ebean.ValuePair;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.Map;

/**
 * Helper to perform a diff given two beans of the same type.
 * <p>
 * This intentionally does not include any OneToMany or ManyToMany properties.
 * </p>
 */
public class DiffHelp {

  private DiffHelp() {
  }

  /**
   * Return a map of the differences between a and b.
   * <p>
   * A and B must be of the same type. B can be null, in which case the 'dirty
   * values' of a is returned.
   * </p>
   * <p>
   * This intentionally does not include as OneToMany or ManyToMany properties.
   * </p>
   */
  public static Map<String, ValuePair> diff(Object newBean, Object oldBean, BeanDescriptor<?> desc) {

    if (!(newBean instanceof EntityBean)) {
      throw new IllegalArgumentException("First bean expected to be an enhanced EntityBean? bean:" + newBean);
    }

    if (oldBean != null) {
      if (!(oldBean instanceof EntityBean)) {
        throw new IllegalArgumentException("Second bean expected to be an enhanced EntityBean? bean:" + oldBean);
      }
      if (!newBean.getClass().isAssignableFrom(oldBean.getClass())) {
        throw new IllegalArgumentException("Second bean not assignable to the first bean?");
      }
    }

    if (oldBean == null) {
      return ((EntityBean) newBean)._ebean_getIntercept().getDirtyValues();
    }

    return desc.diff((EntityBean) newBean, (EntityBean) oldBean);
  }

}

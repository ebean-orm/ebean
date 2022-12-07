package io.ebean.bean.extend;

import io.ebean.bean.EntityBean;
import io.ebean.bean.NotEnhancedException;

/**
 * Marker interface for beans that can be extended with &#64;EntityExtension.
 *
 * @author Roland Praml, FOCONIS AG
 */
public interface ExtendableBean {

  /**
   * Returns an array of registered extensions. This may be useful for bean validation.
   * NOTE: The passed array should NOT be modified.
   */
  default EntityBean[] _ebean_getExtensions() {
    throw new NotEnhancedException();
  }
}

package io.ebean.bean;

import io.ebean.bean.extend.ExtendableBean;

/**
 * Provides access to the EntityExtensions. Each ExtendableBean may have multiple Extension-Accessors stored in the static
 * {@link ExtensionAccessors} per class level.
 * <p>
 * This interface is internally used by the enhancer.
 *
 * @author Roland Praml, FOCONIS AG
 */
public interface ExtensionAccessor {

  /*
   * Returns the extension for a given bean.
   */
  EntityBean getExtension(ExtendableBean bean);

  /**
   * Returns the index of this extension.
   */
  int getIndex();

  /**
   * Return the type of this extension.
   */
  Class<?> getType();

  /**
   * Returns the additional properties of this extension.
   */
  String[] getProperties();
}

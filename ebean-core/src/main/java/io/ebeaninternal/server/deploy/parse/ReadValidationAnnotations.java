package io.ebeaninternal.server.deploy.parse;

import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

/**
 * Reads validation NotNull and Size annotations for mapping.
 */
interface ReadValidationAnnotations {

  /**
   * Return true if the property has a NotNull validation annotation.
   */
  boolean isValidationNotNull(DeployBeanProperty property);

  /**
   * Return the max value of the Size validation annotations on the property.
   */
  int maxSize(DeployBeanProperty property);
}

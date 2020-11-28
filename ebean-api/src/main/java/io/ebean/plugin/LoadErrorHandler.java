package io.ebean.plugin;

import io.ebean.bean.EntityBean;

/**
 * Errorhandler to handle load errors and may be recover correct value.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
@FunctionalInterface
public interface LoadErrorHandler {
  void handleLoadError(EntityBean bean, Property prop, String fullName, Exception e);
}

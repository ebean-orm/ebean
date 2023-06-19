package io.ebean.config.dbplatform;

import io.ebean.DataBindException;

/**
 * Validates a value at bind level. See BindValidatorFactory for details.
 *
 * @author Roland Praml, FOCONIS AG
 */
@FunctionalInterface
public interface BindValidator {

  /**
   * The validate method should throw a DataBindException, if the value is invalid.
   */
  void validate(Object value) throws DataBindException;

}

package io.ebeaninternal.api.filter;

import javax.annotation.Nonnull;

/**
 * Interface for expression test and filtering.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public interface ExpressionTest {

  /**
   * Perform the test with given value
   */
  @Nonnull
  Expression3VL test(@Nonnull Object value);

  /**
   * Test, if the value is null.
   */
  @Nonnull
  default Expression3VL testNull() {
    return Expression3VL.UNKNOWN;
  }

}

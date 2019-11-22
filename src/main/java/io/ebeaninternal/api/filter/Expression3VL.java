package io.ebeaninternal.api.filter;

/**
 * This is required for the 3VL logic, when in-memory filtering is done.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public enum Expression3VL {
  // represents the three values, that can happen on an evaluation.
  FALSE, TRUE, UNKNOWN;


  /**
   * Performs an OR operation.
   *
   * Truth table
   * <ul>
   * <li>FALSE or FALSE = FALSE</li>
   * <li>FALSE or UNKNOWN = UNKNOWN</li>
   * <li>FALSE or TRUE = TRUE</li>
   * <li>UNKNOWN or UNKNOWN = UNKNOWN</li>
   * <li>UNKNOWN or TRUE = TRUE</li>
   * <li>TRUE or TRUE = TRUE</li>
   * </ul>
   *
   */

  public Expression3VL or(Expression3VL other) {
    switch(this) {

    case FALSE:
      return other;

    case TRUE:
      return TRUE;

    default: // UNKNOWN
      return other == TRUE ? TRUE : UNKNOWN;
    }
  }

  /**
   * Performs an AND operation.
   *
   * Truth table
   * <ul>
   * <li>FALSE and FALSE = FALSE</li>
   * <li>FALSE and UNKNOWN = FALSE</li>
   * <li>FALSE and TRUE = FALSE</li>
   * <li>UNKNOWN and UNKNOWN = UNKNOWN</li>
   * <li>UNKNOWN and TRUE = UNKNOWN</li>
   * <li>TRUE and TRUE = TRUE</li>
   * </ul>
   */
  public Expression3VL and(Expression3VL other) {
    switch(this) {

    case FALSE:
      return FALSE;

    case TRUE:
      return other;

    default: // UNKNOWN
      return other == FALSE ? FALSE : UNKNOWN;
    }
  }

  /**
   * Performs a NOT operation.
   *
   * Truth table
   * <ul>
   * <li>not FALSE = TRUE</li>
   * <li>not TRUE = FALSE</li>
   * <li>not UNKNOWN = UNKNOWN</li>
   * </ul>
   */
  public Expression3VL not() {
    switch(this) {

    case FALSE:
      return TRUE;

    case TRUE:
      return FALSE;

    default:
      return UNKNOWN;
    }
  }

}

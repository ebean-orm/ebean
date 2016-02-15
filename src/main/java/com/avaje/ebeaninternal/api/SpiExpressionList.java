package com.avaje.ebeaninternal.api;

import com.avaje.ebean.ExpressionList;

import java.util.List;

/**
 * Internal extension of ExpressionList.
 */
public interface SpiExpressionList<T> extends ExpressionList<T>, SpiExpression {

  /**
   * Return the underlying list of expressions.
   */
  List<SpiExpression> getUnderlyingList();
  
  /**
   * Return a copy of the ExpressionList with the path trimmed for filterMany() expressions.
   */
  SpiExpressionList<?> trimPath(int prefixTrim);
    
	/**
	 * Return true if this list is empty.
	 */
	boolean isEmpty();

}

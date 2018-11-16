package io.ebeaninternal.api.filter;

import java.util.Collection;

/**
 * The filter permuations are internally organized as tree of the FilterContext.
 *
 * You have to fetch the permutation for a assocMany property and the collection of the propertyValue.
 *
 * Then you can call getCurrentValue of the returned object.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public interface FilterPermutations {

  /**
   * traverses to the permutation value.
   */
  FilterPermutations traverse(String propName, Collection<?> src);

  /**
   * Returns the current value of that node.
   */
  Object getCurrentValue();

}

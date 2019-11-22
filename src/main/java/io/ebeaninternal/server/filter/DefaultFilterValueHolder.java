package io.ebeaninternal.server.filter;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import io.ebeaninternal.api.filter.FilterPermutations;

/**
 * ValueHolder for FilterPermutations.
 */
class DefaultFilterValueHolder implements FilterPermutations {

  private final Map<String, DefaultFilterValueHolder> valueHolders = new LinkedHashMap<>();

  Object value;
  Iterator<?> it;
  final Collection<?> src;

  /**
   * Constructor.
   */
  public DefaultFilterValueHolder(Collection<?> src) {
    this.src = src;
    init();
  }

  /**
   *
   */
  private void init() {
    this.it = src.iterator();
    if (it.hasNext()) {
      value = it.next();
    } else {
      value = null;
    }
  }

  /**
   * moves to the next permutation.
   */
  public boolean nextPermutation() {
    for (DefaultFilterValueHolder vh : valueHolders.values()) {
      if (vh.nextPermutation()) {
        return true;
      }
    }

    // Note, that this happens on the deepest valueHolder first
    // in our example we iterate over the "orderDetail" first
    // (which has no valueHolders)
    // if this method returns false, the "order" valueHolder gets
    // its chance to iterate to the next order. In the same step we
    // have to clear the valueHolders, as this map holds the values
    // of the last order
    if (it.hasNext()) {
      value = it.next();
      valueHolders.clear();
      return true;
    } else {
      valueHolders.clear();
      init();
      return false;
    }
  }

  @Override
  public FilterPermutations traverse(String name, Collection<?> src) {
    DefaultFilterValueHolder vh = valueHolders.computeIfAbsent(name, k -> new DefaultFilterValueHolder(src));
    assert vh.src == src;
    return vh;
  }

  @Override
  public Object getCurrentValue() {
    return value;
  }
}
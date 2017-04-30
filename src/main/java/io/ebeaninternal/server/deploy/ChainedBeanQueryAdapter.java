package io.ebeaninternal.server.deploy;

import io.ebean.event.BeanQueryAdapter;
import io.ebean.event.BeanQueryRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Handles multiple BeanQueryAdapter for a given entity type.
 */
public class ChainedBeanQueryAdapter implements BeanQueryAdapter {

  private static final Sorter SORTER = new Sorter();

  private final List<BeanQueryAdapter> list;

  private final BeanQueryAdapter[] chain;

  /**
   * Construct given the list of BeanQueryAdapter's.
   */
  public ChainedBeanQueryAdapter(List<BeanQueryAdapter> list) {
    this.list = list;
    BeanQueryAdapter[] c = list.toArray(new BeanQueryAdapter[list.size()]);
    Arrays.sort(c, SORTER);
    this.chain = c;
  }

  /**
   * Register a new BeanQueryAdapter and return the resulting chain.
   */
  public ChainedBeanQueryAdapter register(BeanQueryAdapter c) {
    if (list.contains(c)) {
      return this;
    } else {
      List<BeanQueryAdapter> newList = new ArrayList<>(list);
      newList.add(c);

      return new ChainedBeanQueryAdapter(newList);
    }
  }

  /**
   * De-register a BeanQueryAdapter and return the resulting chain.
   */
  public ChainedBeanQueryAdapter deregister(BeanQueryAdapter c) {
    if (!list.contains(c)) {
      return this;
    } else {
      List<BeanQueryAdapter> newList = new ArrayList<>(list);
      newList.remove(c);

      return new ChainedBeanQueryAdapter(newList);
    }
  }


  /**
   * Return 0 as not used by this Chained adapter.
   */
  @Override
  public int getExecutionOrder() {
    return 0;
  }

  /**
   * Return false as only individual adapters are registered.
   */
  @Override
  public boolean isRegisterFor(Class<?> cls) {
    return false;
  }

  @Override
  public void preQuery(BeanQueryRequest<?> request) {

    for (BeanQueryAdapter aChain : chain) {
      aChain.preQuery(request);
    }
  }

  /**
   * Helper to order the BeanQueryAdapter's in a chain.
   */
  private static class Sorter implements Comparator<BeanQueryAdapter> {

    @Override
    public int compare(BeanQueryAdapter o1, BeanQueryAdapter o2) {

      int i1 = o1.getExecutionOrder();
      int i2 = o2.getExecutionOrder();
      return (i1 < i2 ? -1 : (i1 == i2 ? 0 : 1));
    }

  }
}

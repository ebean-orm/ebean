package io.ebeaninternal.server.deploy;

import io.ebean.event.BeanPersistListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handles multiple BeanPersistListener's for a given entity type.
 */
public class ChainedBeanPersistListener implements BeanPersistListener {

  private final List<BeanPersistListener> list;

  private final BeanPersistListener[] chain;

  /**
   * Construct adding 2 BeanPersistListener's.
   */
  ChainedBeanPersistListener(BeanPersistListener c1, BeanPersistListener c2) {
    this(addList(c1, c2));
  }

  /**
   * Return the size of the chain.
   */
  protected int size() {
    return chain.length;
  }

  @Override
  public boolean isRegisterFor(Class<?> cls) {
    // never called
    return false;
  }

  /**
   * Helper method used to create a list from 2 BeanPersistListener.
   */
  private static List<BeanPersistListener> addList(BeanPersistListener c1, BeanPersistListener c2) {
    ArrayList<BeanPersistListener> addList = new ArrayList<>(2);
    addList.add(c1);
    addList.add(c2);
    return addList;
  }

  /**
   * Construct given the list of BeanPersistListener's.
   */
  public ChainedBeanPersistListener(List<BeanPersistListener> list) {
    this.list = list;
    this.chain = list.toArray(new BeanPersistListener[list.size()]);
  }

  /**
   * Register a new BeanPersistListener and return the resulting chain.
   */
  public ChainedBeanPersistListener register(BeanPersistListener c) {
    if (list.contains(c)) {
      return this;
    } else {
      List<BeanPersistListener> newList = new ArrayList<>(list);
      newList.add(c);

      return new ChainedBeanPersistListener(newList);
    }
  }

  /**
   * De-register a BeanPersistListener and return the resulting chain.
   */
  public ChainedBeanPersistListener deregister(BeanPersistListener c) {
    if (!list.contains(c)) {
      return this;
    } else {
      List<BeanPersistListener> newList = new ArrayList<>(list);
      newList.remove(c);

      return new ChainedBeanPersistListener(newList);
    }
  }

  @Override
  public void deleted(Object bean) {
    for (BeanPersistListener aChain : chain) {
      aChain.deleted(bean);
    }
  }

  @Override
  public void softDeleted(Object bean) {
    for (BeanPersistListener aChain : chain) {
      aChain.softDeleted(bean);
    }
  }

  @Override
  public void inserted(Object bean) {
    for (BeanPersistListener aChain : chain) {
      aChain.inserted(bean);
    }
  }

  @Override
  public void updated(Object bean, Set<String> updatedProperties) {
    for (BeanPersistListener aChain : chain) {
      aChain.updated(bean, updatedProperties);
    }
  }
}

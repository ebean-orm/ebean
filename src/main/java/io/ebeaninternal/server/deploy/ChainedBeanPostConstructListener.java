package io.ebeaninternal.server.deploy;

import io.ebean.event.BeanPostConstructListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles multiple BeanPostLoad's for a given entity type.
 */
public class ChainedBeanPostConstructListener implements BeanPostConstructListener {

  private final List<BeanPostConstructListener> list;

  private final BeanPostConstructListener[] chain;

  /**
   * Construct given the list of BeanPostCreate's.
   */
  public ChainedBeanPostConstructListener(List<BeanPostConstructListener> list) {
    this.list = list;
    this.chain = list.toArray(new BeanPostConstructListener[list.size()]);
  }

  /**
   * Register a new BeanPostCreate and return the resulting chain.
   */
  public ChainedBeanPostConstructListener register(BeanPostConstructListener c) {
    if (list.contains(c)) {
      return this;
    } else {
      List<BeanPostConstructListener> newList = new ArrayList<>(list);
      newList.add(c);

      return new ChainedBeanPostConstructListener(newList);
    }
  }

  /**
   * De-register a BeanPostCreate and return the resulting chain.
   */
  public BeanPostConstructListener deregister(BeanPostConstructListener c) {
    if (!list.contains(c)) {
      return this;
    } else {
      ArrayList<BeanPostConstructListener> newList = new ArrayList<>(list);
      newList.remove(c);

      return new ChainedBeanPostConstructListener(newList);
    }
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
   * Fire postLoad on all registered BeanPostCreate implementations.
   */
  @Override
  public void postConstruct(Object bean) {
    for (BeanPostConstructListener aChain : chain) {
      aChain.postConstruct(bean);
    }
  }

  @Override
  public void autowire(Object bean) {
    for (BeanPostConstructListener aChain : chain) {
      aChain.autowire(bean);
    }
  }

  @Override
  public void postCreate(Object bean) {
    for (BeanPostConstructListener aChain : chain) {
      aChain.postCreate(bean);
    }
  }
}

package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.event.BeanPostLoad;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles multiple BeanPostLoad's for a given entity type.
 */
public class ChainedBeanPostLoad implements BeanPostLoad {

	private final List<BeanPostLoad> list;

	private final BeanPostLoad[] chain;

	/**
	 * Construct given the list of BeanPersistController's.
	 */
	public ChainedBeanPostLoad(List<BeanPostLoad> list) {
		this.list = list;
		this.chain = list.toArray(new BeanPostLoad[list.size()]);
	}
	
	/**
	 * Register a new BeanPersistController and return the resulting chain.
	 */
	public ChainedBeanPostLoad register(BeanPostLoad c) {
		if (list.contains(c)){
			return this;
		} else {
			List<BeanPostLoad> newList = new ArrayList<BeanPostLoad>();
			newList.addAll(list);
			newList.add(c);
			
			return new ChainedBeanPostLoad(newList);
		}
	}
	
	/**
	 * De-register a BeanPersistController and return the resulting chain.
	 */
	public ChainedBeanPostLoad deregister(BeanPostLoad c) {
		if (!list.contains(c)){
			return this;
		} else {
			ArrayList<BeanPostLoad> newList = new ArrayList<BeanPostLoad>();
			newList.addAll(list);
			newList.remove(c);
			
			return new ChainedBeanPostLoad(newList);
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
   * Fire postLoad on all registered BeanPostLoad implementations.
   */
  @Override
	public void postLoad(Object bean) {
		for (int i = 0; i < chain.length; i++) {
			chain[i].postLoad(bean);
		}
	}
}

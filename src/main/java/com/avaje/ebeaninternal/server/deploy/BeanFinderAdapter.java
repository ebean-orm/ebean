package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.event.BeanFindController;
import com.avaje.ebean.event.BeanFinder;
import com.avaje.ebean.event.BeanQueryRequest;

/**
 * Wraps the old BeanFinder to make it implement BeanFindController.
 */
public class BeanFinderAdapter implements BeanFindController {

  final BeanFinder beanFinder;

  public BeanFinderAdapter(BeanFinder beanFinder) {
    this.beanFinder = beanFinder;
  }

  @Override
  public boolean isRegisterFor(Class<?> cls) {
    return false;
  }

  @Override
  public boolean isInterceptFind(BeanQueryRequest<?> request) {
    // always intercept to support old behavior
    return true;
  }

  @Override
  public <T> T find(BeanQueryRequest<T> request) {
    return (T)beanFinder.find(request);
  }

  @Override
  public <T> BeanCollection<T> findMany(BeanQueryRequest<T> request) {
    return beanFinder.findMany(request);
  }

  @Override
  public boolean isInterceptFindMany(BeanQueryRequest<?> request) {
    // always intercept to support old behavior
    return true;
  }
}

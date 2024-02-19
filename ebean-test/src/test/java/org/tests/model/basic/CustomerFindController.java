package org.tests.model.basic;

import io.ebean.bean.BeanCollection;
import io.ebean.event.BeanFindController;
import io.ebean.event.BeanQueryRequest;

/**
 * @author Noemi Praml, FOCONIS AG
 */
public class CustomerFindController implements BeanFindController  {
  @Override
  public boolean isRegisterFor(Class<?> cls) {
    return Customer.class.isAssignableFrom(cls);
  }

  @Override
  public boolean isInterceptFind(BeanQueryRequest<?> request) {
    return false;
  }

  @Override
  public <T> T find(BeanQueryRequest<T> request) {
    return null;
  }

  @Override
  public boolean isInterceptFindMany(BeanQueryRequest<?> request) {
    return false;
  }

  @Override
  public <T> BeanCollection<T> findMany(BeanQueryRequest<T> request) {
    return null;
  }
}

package io.ebeaninternal.server.deploy;

import io.ebean.bean.BeanCollection;

import java.util.Collection;

abstract class BaseCollectionHelp<T> implements BeanCollectionHelp<T> {

  @Override
  public Collection underlying(Object value) {
    if (value instanceof BeanCollection) {
      return ((BeanCollection)value).getActualDetails();
    } else {
      return (Collection)value;
    }
  }
}

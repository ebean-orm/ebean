package org.tests.delete;

import io.ebean.event.AbstractBeanPersistListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DcListener extends AbstractBeanPersistListener {

  static List<Object> deleted = Collections.synchronizedList(new ArrayList<>());

  @Override
  public boolean isRegisterFor(Class<?> cls) {
    return cls.equals(DcDetail.class);
  }

  @Override
  public void deleted(Object bean) {
    deleted.add(bean);
  }

  static List<Object> deletedBeans() {
    List<Object> copy = new ArrayList<>(deleted);
    deleted.clear();
    return copy;
  }
}

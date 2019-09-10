package org.tests.model.elementcollection;

import io.ebean.event.BeanPersistAdapter;
import io.ebean.event.BeanPersistRequest;

import java.util.ArrayList;
import java.util.List;

public class EcPersonPersistAdapter extends BeanPersistAdapter {

  private static List<String> LOG = new ArrayList<>();

  static List<String> eventLog() {
    List copy = new ArrayList(LOG);
    LOG.clear();
    return copy;
  }

  @Override
  public boolean isRegisterFor(Class<?> cls) {
    return cls.equals(EcPerson.class);
  }


  @Override
  public boolean preInsert(BeanPersistRequest<?> request) {
    LOG.add("preInsert");
    return true;
  }

  @Override
  public void postInsert(BeanPersistRequest<?> request) {
    LOG.add("postInsert");
  }

  @Override
  public boolean preUpdate(BeanPersistRequest<?> request) {
    LOG.add("preUpdate");
    return true;
  }

  @Override
  public void postUpdate(BeanPersistRequest<?> request) {
    LOG.add("postUpdate");
  }

  @Override
  public boolean preDelete(BeanPersistRequest<?> request) {
    LOG.add("preDelete");
    return true;
  }

  @Override
  public void postDelete(BeanPersistRequest<?> request) {
    LOG.add("postDelete");
  }

}

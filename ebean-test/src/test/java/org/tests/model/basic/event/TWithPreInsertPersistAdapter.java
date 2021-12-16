package org.tests.model.basic.event;

import io.ebean.event.BeanPersistAdapter;
import io.ebean.event.BeanPersistRequest;
import org.tests.model.basic.TWithPreInsertCommon;

public class TWithPreInsertPersistAdapter extends BeanPersistAdapter {

  @Override
  public boolean isRegisterFor(Class<?> cls) {
    return TWithPreInsertCommon.class.isAssignableFrom(cls);
  }

  @Override
  public boolean preInsert(BeanPersistRequest<?> request) {
    TWithPreInsertCommon bean = (TWithPreInsertCommon) request.bean();
    if (bean.getName() == null) {
      bean.setName("set on preInsert");
    }
    bean.requestCascadeState(request.isCascade() ? 1 : 2);
    return true;
  }

  @Override
  public boolean preUpdate(BeanPersistRequest<?> request) {
    TWithPreInsertCommon bean = (TWithPreInsertCommon) request.bean();
    System.out.println("preUpdate - title is: " + bean.getTitle());
    if (bean.getTitle() == null) {
      bean.setTitle("set on preUpdate");
    }
    bean.requestCascadeState(request.isCascade() ? 11 : 12);
    return super.preUpdate(request);
  }

}

package org.tests.model.basic.event;

import io.ebean.event.BeanPersistAdapter;
import io.ebean.event.BeanPersistRequest;
import org.tests.model.basic.TWithPreInsert;

public class TWithPreInsertPersistAdapter extends BeanPersistAdapter {

  @Override
  public boolean isRegisterFor(Class<?> cls) {
    return TWithPreInsert.class.equals(cls);
  }

  @Override
  public boolean preInsert(BeanPersistRequest<?> request) {

    TWithPreInsert e = (TWithPreInsert) request.getBean();
    if (e.getName() == null) {
      e.setName("set on preInsert");
    }
    return true;
  }

  @Override
  public boolean preUpdate(BeanPersistRequest<?> request) {

    TWithPreInsert b = (TWithPreInsert) request.getBean();
    System.out.println("preUpdate - title is: " + b.getTitle());
    if (b.getTitle() == null) {
      b.setTitle("set on preUpdate");
    }
    // request.getEbeanServer().refresh(b);
    // System.out.println("title is Mister:"+b.getTitle());

    return super.preUpdate(request);
  }

}

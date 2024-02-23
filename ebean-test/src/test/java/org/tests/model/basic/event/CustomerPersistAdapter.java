package org.tests.model.basic.event;

import io.ebean.DB;
import io.ebean.event.BeanPersistAdapter;
import io.ebean.event.BeanPersistRequest;
import org.tests.model.basic.Customer;
import org.tests.model.basic.TSMaster;

public class CustomerPersistAdapter extends BeanPersistAdapter {

  @Override
  public boolean isRegisterFor(Class<?> cls) {
    return Customer.class.equals(cls);
  }

  @Override
  public boolean preInsert(BeanPersistRequest<?> request) {

//		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
//		request.getTransaction().log("+++++ "+Arrays.toString(stackTrace));
    if (((Customer) request.bean()).getName().startsWith("BatchFlushPreInsert")) {
      DB.find(TSMaster.class).where().eq("name", "master1").exists();
    }
    return true;
  }

  @Override
  public boolean preUpdate(BeanPersistRequest<?> request) {

    // Do nothing intentionally. TestStatelessUpdate needs
    // to control if customer contacts is 'touched'
    return true;
  }

  @Override
  public void postInsert(BeanPersistRequest<?> request) {
    super.postInsert(request);
    if (((Customer) request.bean()).getName().startsWith("BatchFlushPostInsert")) {
      DB.find(TSMaster.class).where().eq("name", "master1").exists();
    }
  }
}

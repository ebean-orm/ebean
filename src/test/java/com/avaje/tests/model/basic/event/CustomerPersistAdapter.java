package com.avaje.tests.model.basic.event;

import com.avaje.ebean.event.BeanPersistAdapter;
import com.avaje.ebean.event.BeanPersistRequest;
import com.avaje.tests.model.basic.Customer;

public class CustomerPersistAdapter extends BeanPersistAdapter {

	@Override
	public boolean isRegisterFor(Class<?> cls) {
		return Customer.class.equals(cls);
	}

	@Override
	public boolean preInsert(BeanPersistRequest<?> request) {
						
//		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
//		request.getTransaction().log("+++++ "+Arrays.toString(stackTrace));
		
		return true;
	}

  @Override
  public boolean preUpdate(BeanPersistRequest<?> request) {

    Customer customer = (Customer) request.getBean();

    if (customer.getContacts() != null) {
      String test = "accessed";
    }

    return true;
  }

}

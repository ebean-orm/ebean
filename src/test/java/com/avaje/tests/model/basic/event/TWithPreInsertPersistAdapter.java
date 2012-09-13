package com.avaje.tests.model.basic.event;

import com.avaje.ebean.event.BeanPersistAdapter;
import com.avaje.ebean.event.BeanPersistRequest;
import com.avaje.tests.model.basic.TWithPreInsert;

public class TWithPreInsertPersistAdapter extends BeanPersistAdapter {

	@Override
	public boolean isRegisterFor(Class<?> cls) {
		return TWithPreInsert.class.equals(cls);
	}

	@Override
	public boolean preInsert(BeanPersistRequest<?> request) {
		
		TWithPreInsert e = (TWithPreInsert)request.getBean();
		
		e.setName("aname");
		return true;
	}

	@Override
    public boolean preUpdate(BeanPersistRequest<?> request) {
	    
		TWithPreInsert b = (TWithPreInsert)request.getBean();
		System.out.println("title is Missus:"+b.getTitle());
		
		//Ebean.refresh(b);
		request.getEbeanServer().refresh(b);
		System.out.println("title is Mister:"+b.getTitle());
		
		return super.preUpdate(request);
    }

	
	
	
}

package com.avaje.tests.model.basic.event;

import com.avaje.ebean.Query;
import com.avaje.ebean.event.BeanQueryAdapter;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.tests.model.basic.TOne;

public class SimpleTOneQueryAdapter implements BeanQueryAdapter {

	public int getExecutionOrder() {
		// just return 0
		return 0;
	}

	public boolean isRegisterFor(Class<?> cls) {
		return TOne.class.equals(cls);
	}

	public void preQuery(BeanQueryRequest<?> request) {
		
		Query<?> query = request.getQuery();
		
		// can get the type of query Bean, List, RowCount, Id's etc
		//Type queryType = query.getType();
		
		query.where().raw("1=1");
	}

	
}

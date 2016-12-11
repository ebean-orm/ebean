package org.tests.model.basic.event;

import io.ebean.Query;
import io.ebean.event.BeanQueryAdapter;
import io.ebean.event.BeanQueryRequest;
import org.tests.model.basic.TOne;

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

package org.tests.model.basic.event;

import io.ebean.Query;
import io.ebean.event.BeanQueryAdapter;
import io.ebean.event.BeanQueryRequest;
import org.tests.model.basic.TOne;

public class SimpleTOneQueryAdapter implements BeanQueryAdapter {

  @Override
  public int getExecutionOrder() {
    // just return 0
    return 0;
  }

  @Override
  public boolean isRegisterFor(Class<?> cls) {
    return TOne.class.equals(cls);
  }

  @Override
  public void preQuery(BeanQueryRequest<?> request) {

    Query<?> query = request.getQuery();

    switch (query.getQueryType()) {
      case DELETE: {
        query.where().raw("3=3");
        break;
      }
      case UPDATE: {
        query.where().raw("2=2");
        break;
      }
      case FIND: {
        query.where().raw("1=1");
        break;
      }
    }
  }


}

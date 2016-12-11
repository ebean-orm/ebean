package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.MyLobSize;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class TestDefaultFetchLazy extends BaseTestCase {

  @Test
  public void testFetchTypeLazy() {

    MyLobSize m = new MyLobSize();
    m.setName("aname");
    m.setMyCount(10);
    m.setMyLob("A big lob of data");

    Ebean.save(m);

    Assert.assertNotNull(m.getId());

    MyLobSize myLobSize = Ebean.find(MyLobSize.class, m.getId());

    BeanState beanState = Ebean.getBeanState(myLobSize);
    Set<String> loadedProps = beanState.getLoadedProps();

    Assert.assertNotNull(loadedProps);
    Assert.assertTrue(loadedProps.contains("id"));
    Assert.assertTrue(loadedProps.contains("name"));

    // FetchType.LAZY properties excluded
    Assert.assertFalse(loadedProps.contains("myLob"));
    Assert.assertFalse(loadedProps.contains("myCount"));

    // the details is also tuned
    Query<MyLobSize> queryMany = Ebean.find(MyLobSize.class).fetch("details")// ,"+query")
      .where().gt("id", 0).query();

    queryMany.findList();

    String generatedSql = queryMany.getGeneratedSql();
    Assert.assertTrue(generatedSql.contains("t1.other"));
    Assert.assertFalse(generatedSql.contains("t1.something"));
  }

}

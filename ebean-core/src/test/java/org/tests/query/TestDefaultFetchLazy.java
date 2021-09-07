package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import io.ebean.Query;
import org.tests.model.basic.MyLobSize;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestDefaultFetchLazy extends BaseTestCase {

  @Test
  public void testFetchTypeLazy() {

    MyLobSize m = new MyLobSize();
    m.setName("aname");
    m.setMyCount(10);
    m.setMyLob("A big lob of data");

    DB.save(m);

    assertNotNull(m.getId());

    MyLobSize myLobSize = DB.find(MyLobSize.class, m.getId());

    BeanState beanState = DB.getBeanState(myLobSize);
    Set<String> loadedProps = beanState.getLoadedProps();

    assertNotNull(loadedProps);
    assertTrue(loadedProps.contains("id"));
    assertTrue(loadedProps.contains("name"));

    // FetchType.LAZY properties excluded
    assertFalse(loadedProps.contains("myLob"));
    assertFalse(loadedProps.contains("myCount"));

    // the details is also tuned
    Query<MyLobSize> queryMany = DB.find(MyLobSize.class).fetch("details")// ,"+query")
      .where().gt("id", 0).query();

    queryMany.findList();

    String generatedSql = queryMany.getGeneratedSql();
    assertTrue(generatedSql.contains("t1.other"));
    assertFalse(generatedSql.contains("t1.something"));
  }

}

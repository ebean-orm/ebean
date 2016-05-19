package com.avaje.tests.rawsql.inherit;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import org.avaje.test.model.rawsql.inherit.ChildA;
import org.avaje.test.model.rawsql.inherit.ChildB;
import org.avaje.test.model.rawsql.inherit.Data;
import org.avaje.test.model.rawsql.inherit.Parent;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ParentQueryTest extends BaseTestCase {

  @Test
  public void QueryParentCollectionFetch() {

    List<Data> exampleData = new ArrayList<Data>();
    exampleData.add(new Data(0));
    exampleData.add(new Data(1));
    exampleData.add(new Data(2));

    ChildA a = new ChildA(0, "PA");
    a.setData(exampleData);
    Ebean.save(a);

    ChildB b = new ChildB(1, "PB");
    b.setData(exampleData);
    Ebean.save(b);

    ChildA c = new ChildA(2, "PC");
    c.setData(exampleData);
    Ebean.save(c);

    List<Parent> partial = Ebean.find(Parent.class).where().ge("val", 1).findList();
    assertNotNull(partial.get(0).getData());
    assertEquals(partial.get(0).getData().get(0).getVal().intValue(), 0);
  }

}

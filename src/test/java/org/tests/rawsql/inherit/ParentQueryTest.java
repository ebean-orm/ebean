package org.tests.rawsql.inherit;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.inherit.ChildA;
import org.tests.inherit.ChildB;
import org.tests.inherit.Data;
import org.tests.inherit.Parent;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ParentQueryTest extends BaseTestCase {

  @Before
  public void clearDb() {
    Ebean.deleteAll(Ebean.find(Data.class).findList());
    //@rob: this does not work as it does not clear the ManyToMany relations.
    //Ebean.find(Data.class).delete(); 
    Ebean.find(Parent.class).delete();
  }
  
  @Test
  public void QueryParentCollectionFetch() {

    List<Data> exampleData = new ArrayList<>();
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

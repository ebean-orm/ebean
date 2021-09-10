package org.tests.rawsql.inherit;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.inherit.ChildA;
import org.tests.inherit.ChildB;
import org.tests.inherit.Data;
import org.tests.inherit.Parent;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParentQueryTest extends BaseTestCase {

  @Test
  public void QueryParentCollectionFetch() {

    List<Data> exampleData = new ArrayList<>();
    exampleData.add(new Data(0));
    exampleData.add(new Data(1));
    exampleData.add(new Data(2));

    ChildA a = new ChildA(1000, "PQT-PA");
    a.setData(exampleData);
    DB.save(a);

    ChildB b = new ChildB(1001, "PQT-PB");
    b.setData(exampleData);
    DB.save(b);

    ChildA c = new ChildA(1002, "PQT-PC");
    c.setData(exampleData);
    DB.save(c);

    List<Parent> partial = DB.find(Parent.class).where().ge("val", 1001).findList();
    assertNotNull(partial.get(0).getData());
    assertThat(partial.get(0).getMore()).startsWith("PQT-");
    assertEquals(partial.get(0).getData().get(0).getVal().intValue(), 0);
  }

}

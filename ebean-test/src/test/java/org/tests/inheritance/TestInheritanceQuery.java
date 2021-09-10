package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.inherit.ChildA;
import org.tests.inherit.ChildB;
import org.tests.inherit.Parent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInheritanceQuery extends BaseTestCase {

  @Test
  public void test() {

    Parent a = new ChildA(90, "Bean A");
    Parent b1 = new ChildB(91, "Bean B1");
    Parent b2 = new ChildB(92, "Bean B2");

    server().save(a);
    server().save(b1);
    server().save(b2);


    LoggedSql.start();

    Query<Parent> query = DB.find(Parent.class);

    query.where().in("val", 90, 91); // restrict to a & b1

    assertThat(query.findList()).hasSize(2); // a & b1

    Query<Parent> query2 = query.copy();
    query2.setInheritType(ChildA.class);

    assertThat(query2.findList()).containsExactly(a);

    query2.setInheritType(ChildB.class);
    assertThat(query2.findList()).containsExactly(b1);

    query2 = DB.find(Parent.class).setInheritType(ChildB.class);
    assertThat(query2.findList()).contains(b1, b2);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(4);
    assertSql(sql.get(1)).contains("where t0.type = 'A'");
    assertSql(sql.get(2)).contains("where t0.type = 'B'");
    assertThat(sql.get(3)).contains("where t0.type = 'B'");

    DB.delete(a);
    DB.delete(b1);
    DB.delete(b2);
  }
}

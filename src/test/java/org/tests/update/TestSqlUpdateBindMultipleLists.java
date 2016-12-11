package org.tests.update;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class TestSqlUpdateBindMultipleLists extends BaseTestCase {

  @Test
  public void test() {

    SqlUpdate sqlUpdate = Ebean.createSqlUpdate("delete from o_customer where id in (:ids)");

    sqlUpdate.setParameter("ids", asList(9991, 9992, 9993));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?,?,?)", sqlUpdate.getGeneratedSql());

    // 3 parameters in the IN clause
    sqlUpdate.setParameter("ids", asList(9991, 9992));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?,?)", sqlUpdate.getGeneratedSql());

  }


  @Test
  public void test_multipleLists() {

    SqlUpdate sqlUpdate = Ebean.createSqlUpdate("delete from o_customer where id in (:ids) and name in (:names)");

    sqlUpdate.setParameter("ids", asList(9991, 9992, 9993));
    sqlUpdate.setParameter("names", asList("rob", "jim"));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?,?,?) and name in (?,?)", sqlUpdate.getGeneratedSql());

    sqlUpdate.setParameter("ids", asList(9991, 9992));
    sqlUpdate.setParameter("names", asList("rob", "jim", "sd"));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?,?) and name in (?,?,?)", sqlUpdate.getGeneratedSql());

    sqlUpdate.setParameter("ids", asList(9991, 9992));
    sqlUpdate.setParameter("names", asList("rob", "jim"));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?,?) and name in (?,?)", sqlUpdate.getGeneratedSql());

    sqlUpdate.setParameter("ids", asList(9991));
    sqlUpdate.setParameter("names", asList("rob", "jim"));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?) and name in (?,?)", sqlUpdate.getGeneratedSql());

    sqlUpdate.setParameter("ids", asList(9992));
    sqlUpdate.setParameter("names", asList("ro3b", "j3im"));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?) and name in (?,?)", sqlUpdate.getGeneratedSql());

    sqlUpdate.setParameter("ids", asList(9992, 4545));
    sqlUpdate.setParameter("names", asList("ro3b"));
    sqlUpdate.execute();
    assertEquals("delete from o_customer where id in (?,?) and name in (?)", sqlUpdate.getGeneratedSql());

  }

}

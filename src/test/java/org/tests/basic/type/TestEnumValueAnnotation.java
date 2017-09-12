package org.tests.basic.type;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.EBasic.Status;
import org.tests.model.basic.EBasicEnumId;
import org.tests.model.basic.EBasicEnumInt;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestEnumValueAnnotation extends BaseTestCase {

  @Test
  public void test() {

    EBasic b = new EBasic();
    b.setName("Banana");
    b.setStatus(Status.NEW);

    Ebean.save(b);

    SqlQuery q = Ebean.createSqlQuery("select * from e_basic where id = :id");
    q.setParameter("id", b.getId());

    SqlRow sqlRow = q.findOne();
    String strStatus = sqlRow.getString("status");

    assertEquals("N", strStatus);

    EBasic b2 = new EBasic();
    b2.setName("Apple");
    b2.setStatus(Status.NEW);

    Ebean.save(b2);

    EBasic b3 = Ebean.find(EBasic.class, b2.getId());
    b3.setName("Orange");

    Ebean.save(b3);
  }

  @Test
  public void testAsId() {
    EBasicEnumId b = new EBasicEnumId();
    b.setName("Banana");
    b.setStatus(EBasicEnumId.Status.NEW);

    Ebean.save(b);

    SqlQuery q = Ebean.createSqlQuery("select * from e_basic_enum_id where status = :status");
    q.setParameter("status", b.getStatus());

    SqlRow sqlRow = q.findOne();
    String strStatus = sqlRow.getString("status");

    assertEquals("N", strStatus);

    try {
      b = Ebean.find(EBasicEnumId.class, b.getStatus());
    } catch (java.lang.IllegalArgumentException iae) {
      Assert.fail("The use of an enum as id should work : " + iae.getLocalizedMessage());
    }

    assertEquals(EBasicEnumId.Status.NEW, b.getStatus());
  }

  @Test
  public void testDbEnumValueInt() {

    EBasicEnumInt b = new EBasicEnumInt();
    b.setName("Banana");
    b.setStatus(EBasicEnumInt.Status.NEW);

    Ebean.save(b);

    SqlQuery q = Ebean.createSqlQuery("select * from e_basic_eni where id = :id");
    q.setParameter("id", b.getId());

    Optional<SqlRow> sqlRow = q.findOneOrEmpty();
    sqlRow.ifPresent(sqlRow1 -> {
      Integer intStatus = sqlRow1.getInteger("status");
      assertEquals(Integer.valueOf(1), intStatus);
    });


    EBasicEnumInt b2 = Ebean.find(EBasicEnumInt.class)
      .where().eq("id", b.getId())
      .eq("status", EBasicEnumInt.Status.NEW)
      .findOne();

    assertNotNull(b2);
  }
}

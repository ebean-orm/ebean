package com.avaje.tests.basic.type;

import com.avaje.tests.model.basic.EBasicEnumInt;
import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.tests.model.basic.EBasic;
import com.avaje.tests.model.basic.EBasic.Status;
import com.avaje.tests.model.basic.EBasicEnumId;

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

    SqlRow sqlRow = q.findUnique();
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

    SqlRow sqlRow = q.findUnique();
    String strStatus = sqlRow.getString("status");

    assertEquals("N", strStatus);

    try {
      b = Ebean.find(EBasicEnumId.class, b.getStatus());
    } catch (IllegalArgumentException iae) {
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

    SqlRow sqlRow = q.findUnique();
    Integer intStatus = sqlRow.getInteger("status");

    assertEquals(Integer.valueOf(1), intStatus);


    EBasicEnumInt b2 = Ebean.find(EBasicEnumInt.class)
        .where().eq("id", b.getId())
        .eq("status", EBasicEnumInt.Status.NEW)
        .findUnique();

    assertNotNull(b2);
  }
}

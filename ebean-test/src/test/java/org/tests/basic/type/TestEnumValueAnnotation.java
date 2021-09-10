package org.tests.basic.type;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.EBasic.Status;
import org.tests.model.basic.EBasicEnumId;
import org.tests.model.basic.EBasicEnumInt;

import java.util.Optional;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestEnumValueAnnotation extends BaseTestCase {

  @Test
  public void test() {

    EBasic b = new EBasic();
    b.setName("Banana");
    b.setStatus(Status.NEW);

    DB.save(b);

    SqlRow sqlRow = DB.sqlQuery("select * from e_basic where id = :id")
      .setParameter("id", b.getId())
      .findOne();

    String strStatus = sqlRow.getString("status");
    assertEquals("N", strStatus);

    EBasic b2 = new EBasic();
    b2.setName("Apple");
    b2.setStatus(Status.NEW);

    DB.save(b2);

    EBasic b3 = DB.find(EBasic.class, b2.getId());
    b3.setName("Orange");

    DB.save(b3);
  }

  @Test
  public void testAsId() {
    EBasicEnumId b = new EBasicEnumId();
    b.setName("Banana");
    b.setStatus(EBasicEnumId.Status.NEW);

    DB.save(b);

    SqlQuery q = DB.sqlQuery("select * from e_basic_enum_id where status = :status");
    q.setParameter("status", b.getStatus());

    SqlRow sqlRow = q.findOne();
    String strStatus = sqlRow.getString("status");

    assertEquals("N", strStatus);

    try {
      b = DB.find(EBasicEnumId.class, b.getStatus());
    } catch (java.lang.IllegalArgumentException iae) {
      fail("The use of an enum as id should work : " + iae.getLocalizedMessage());
    }

    assertEquals(EBasicEnumId.Status.NEW, b.getStatus());
  }

  @Test
  public void testDbEnumValueInt() {

    EBasicEnumInt b = new EBasicEnumInt();
    b.setName("Banana");
    b.setStatus(EBasicEnumInt.Status.NEW);

    DB.save(b);

    SqlQuery q = DB.sqlQuery("select * from e_basic_eni where id = :id");
    q.setParameter("id", b.getId());

    Optional<SqlRow> sqlRow = q.findOneOrEmpty();
    sqlRow.ifPresent(sqlRow1 -> {
      Integer intStatus = sqlRow1.getInteger("status");
      assertEquals(Integer.valueOf(1), intStatus);
    });


    EBasicEnumInt b2 = DB.find(EBasicEnumInt.class)
      .where().eq("id", b.getId())
      .eq("status", EBasicEnumInt.Status.NEW)
      .findOne();

    assertNotNull(b2);
  }
}

package com.avaje.tests.basic.type;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.tests.model.basic.TUuidEntity;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class TestSqlRowUUID extends BaseTestCase {

  @Test
  public void test() {

    TUuidEntity e = new TUuidEntity();
    e.setName("blah");

    Ebean.save(e);

    SqlQuery q = Ebean.createSqlQuery("select * from tuuid_entity where id = :id");
    q.setParameter("id", e.getId());
    SqlRow sqlRow = q.findUnique();

    UUID id = sqlRow.getUUID("id");

    Assert.assertNotNull(id);

    Boolean b = sqlRow.getBoolean("name");
    Assert.assertFalse(b);
  }
}

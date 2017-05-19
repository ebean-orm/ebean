package org.tests.basic.encrypt;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import org.tests.model.basic.EBasicEncryptBinary;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Timestamp;

public class TestEncryptBinary extends BaseTestCase {

  @Test
  public void test() {

    Timestamp t0 = new Timestamp(System.currentTimeMillis());
    EBasicEncryptBinary e = new EBasicEncryptBinary();
    e.setName("test1");
    e.setDescription("testdesc1");
    e.setSomeTime(t0);
    e.setData("HelloWorld".getBytes());

    Ebean.save(e);

    SqlQuery q = Ebean.createSqlQuery("select * from e_basicenc_bin where id = :id");
    q.setParameter("id", e.getId());

    SqlRow row = q.findOne();
    row.getString("name");
    row.get("data");
    row.get("some_time");

    EBasicEncryptBinary e1 = Ebean.find(EBasicEncryptBinary.class, e.getId());

    Timestamp t1 = e1.getSomeTime();
    e1.getData();

    e1.getDescription();

    Assert.assertEquals(t0, t1);

    e1.setName("testmod");
    e1.setDescription("moddesc");

    Ebean.save(e1);

    EBasicEncryptBinary e2 = Ebean.find(EBasicEncryptBinary.class, e.getId());
    e2.getDescription();
  }

}

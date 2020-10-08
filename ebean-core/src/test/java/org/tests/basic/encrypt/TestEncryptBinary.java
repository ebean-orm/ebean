package org.tests.basic.encrypt;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.SqlRow;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.basic.EBasicEncryptBinary;

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

    DB.save(e);

    SqlRow row = DB.createSqlQuery("select * from e_basicenc_bin where id = :id")
      .setParameter("id", e.getId())
      .findOne();

    row.getString("name");
    row.get("data");
    row.get("some_time");

    EBasicEncryptBinary e1 = DB.find(EBasicEncryptBinary.class, e.getId());

    Timestamp t1 = e1.getSomeTime();
    e1.getData();

    e1.getDescription();

    Assert.assertEquals(t0, t1);

    e1.setName("testmod");
    e1.setDescription("moddesc");

    DB.save(e1);

    EBasicEncryptBinary e2 = DB.find(EBasicEncryptBinary.class, e.getId());
    e2.getDescription();
  }

}

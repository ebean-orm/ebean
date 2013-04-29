package com.avaje.tests.basic.encrypt;

import java.sql.Timestamp;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.tests.model.basic.EBasicEncryptBinary;

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

    SqlRow row = q.findUnique();
    String name = row.getString("name");
    Object data = row.get("data");
    Object someTimeData = row.get("some_time");
    System.out.println("SqlRow name:" + name + " data:" + data + " someTime:" + someTimeData);

    EBasicEncryptBinary e1 = Ebean.find(EBasicEncryptBinary.class, e.getId());

    Timestamp t1 = e1.getSomeTime();
    byte[] data1 = e1.getData();
    String s = new String(data1);
    String desc1 = e1.getDescription();
    System.out.println("Decrypted data:" + s + " desc:" + desc1);

    Assert.assertEquals(t0, t1);

    e1.setName("testmod");
    e1.setDescription("moddesc");

    Ebean.save(e1);

    EBasicEncryptBinary e2 = Ebean.find(EBasicEncryptBinary.class, e.getId());

    String desc2 = e2.getDescription();
    System.out.println("moddesc=" + desc2);
  }

}

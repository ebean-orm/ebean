package com.avaje.ebeaninternal.server.type;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.avaje.tests.model.basic.UUOne;

public class TestBinaryUUID extends BaseTestCase {

  @Test
  public void test() {
    
   
    UUOne one0 = new UUOne();
    one0.setName("first one");

    UUID id = UUID.randomUUID();
    UUOne one1 = new UUOne();
    one1.setId(id);
    one1.setName("second one");

    Ebean.save(one0);
    Ebean.save(one1);
    
    UUOne fetch0 = Ebean.find(UUOne.class, one0.getId());
    UUOne fetch1 = Ebean.find(UUOne.class, one1.getId());
    
    Assert.assertEquals(one0.getId(), fetch0.getId());
    Assert.assertEquals(one0.getName(), fetch0.getName());

    Assert.assertEquals(one1.getId(), fetch1.getId());
    Assert.assertEquals(one1.getName(), fetch1.getName());

    String sql = "select id, name from uuone";
    List<SqlRow> list = Ebean.createSqlQuery(sql).findList();
    for (SqlRow sqlRow : list) {
      Object sqlId = sqlRow.get("id");
      Assert.assertNotNull(sqlId);
      Assert.assertTrue(sqlId instanceof byte[]);
      UUID uuid = sqlRow.getUUID("id");
      Assert.assertNotNull(uuid);
    }
    
  }
  
}

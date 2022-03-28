package io.ebean.xtest.core;

import io.ebean.DB;
import io.ebean.SqlRow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.UUOne;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestBinaryUUID {

  @Test
  public void test() {

    UUOne one0 = new UUOne();
    one0.setName("first one");

    UUID id = UUID.randomUUID();
    UUOne one1 = new UUOne();
    one1.setId(id);
    one1.setName("second one");

    DB.save(one0);
    DB.save(one1);

    UUOne fetch0 = DB.find(UUOne.class, one0.getId());
    UUOne fetch1 = DB.find(UUOne.class, one1.getId());

    Assertions.assertEquals(one0.getId(), fetch0.getId());
    Assertions.assertEquals(one0.getName(), fetch0.getName());

    Assertions.assertEquals(one1.getId(), fetch1.getId());
    Assertions.assertEquals(one1.getName(), fetch1.getName());

    String sql = "select id, name from uuone";
    List<SqlRow> list = DB.sqlQuery(sql).findList();
    for (SqlRow sqlRow : list) {
      Object sqlId = sqlRow.get("id");
      assertNotNull(sqlId);
      UUID uuid = sqlRow.getUUID("id");
      assertNotNull(uuid);
    }


    String asJson = DB.json().toJson(fetch0);
    UUOne bean = DB.json().toBean(UUOne.class, asJson);

    Assertions.assertEquals(fetch0.getId(), bean.getId());
    Assertions.assertEquals(fetch0.getName(), bean.getName());
  }

}

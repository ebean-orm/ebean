package io.ebeaninternal.server.type;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlRow;
import org.tests.model.basic.UUOne;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    assertEquals(one0.getId(), fetch0.getId());
    assertEquals(one0.getName(), fetch0.getName());

    assertEquals(one1.getId(), fetch1.getId());
    assertEquals(one1.getName(), fetch1.getName());

    String sql = "select id, name from uuone";
    List<SqlRow> list = Ebean.createSqlQuery(sql).findList();
    for (SqlRow sqlRow : list) {
      Object sqlId = sqlRow.get("id");
      assertNotNull(sqlId);
      UUID uuid = sqlRow.getUUID("id");
      assertNotNull(uuid);
    }


    String asJson = Ebean.json().toJson(fetch0);
    UUOne bean = Ebean.json().toBean(UUOne.class, asJson);

    assertEquals(fetch0.getId(), bean.getId());
    assertEquals(fetch0.getName(), bean.getName());
  }

}

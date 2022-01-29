package org.tests.json;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import io.ebean.text.json.EJson;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.tests.model.json.EBasicJsonMap;
import org.tests.model.json.EBasicJsonMapDetail;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJsonMapBasic extends BaseTestCase {

  private final EBasicJsonMap bean = new EBasicJsonMap();;

  @AfterEach
  void cleanup() {
    if (bean != null && bean.getId() != null) {
      DB.delete(EBasicJsonMap.class, bean.getId());
    }
  }

  @Test
  @ForPlatform(Platform.POSTGRES)
  public void whereManyPredicatePg() {

    bean.setName("own1");
    bean.getDetails().add(new EBasicJsonMapDetail("detail1"));
    bean.getDetails().add(new EBasicJsonMapDetail("detail2"));

    DB.save(bean);

    Query<EBasicJsonMap> query1 = DB.find(EBasicJsonMap.class)
      .fetch("details")
      .where().startsWith("details.name", "detail")
      .query();

    query1.findList();

    assertThat(query1.getGeneratedSql()).contains("select distinct on (t0.id, t1.id) ");

    Query<EBasicJsonMap> query2 = DB.find(EBasicJsonMap.class)
      .where().startsWith("details.name", "detail")
      .query();
    query2.findList();

    assertThat(query2.getGeneratedSql()).contains("select distinct on (t0.id) ");
  }

  @Test
  @ForPlatform(Platform.DB2)
  public void whereManyPredicateDb2() {

    Map<String, Object> m1 = new HashMap<>();
    m1.put("foo", "bar");
    bean.setContent(m1);
    bean.setName("own1");
    bean.getDetails().add(new EBasicJsonMapDetail("db2-detail1"));
    bean.getDetails().add(new EBasicJsonMapDetail("db2-detail2"));

    DB.save(bean);
    Query<EBasicJsonMap> query1 = DB.find(EBasicJsonMap.class).select("*").fetch("details").where()
        .startsWith("details.name", "db2-detail").query();

    List<EBasicJsonMap> lst = query1.findList();

    assertThat(query1.getGeneratedSql()).contains("select distinct t0.id, t0.name, t0.version,")
        .doesNotContain("content");
    assertThat(lst).hasSize(1);
    assertThat(lst.get(0).getContent()).containsEntry("foo", "bar");

    Query<EBasicJsonMap> query2 = DB.find(EBasicJsonMap.class).where().startsWith("details.name", "db2-detail")
        .query();
    query2.findList();

    assertThat(query2.getGeneratedSql()).contains("select distinct t0.id, t0.name, t0.version from");

  }

  @Test
  public void testInsertUpdateDelete() throws IOException {

    String s0 = "{\"docId\":18,\"contentId\":\"asd\",\"active\":true,\"contentType\":\"pg-hello\",\"content\":{\"name\":\"rob\",\"age\":45}}";
    //String s1 = "{\"docId\":19,\"contentId\":\"asd\",\"active\":true,\"contentType\":\"pg-hello\",\"content\":{\"name\":\"rob\",\"age\":45}}";

    Map<String, Object> content = EJson.parseObject(s0);

    bean.setName("one");
    bean.setContent(content);

    DB.save(bean);

    EBasicJsonMap bean1 = DB.find(EBasicJsonMap.class, bean.getId());

    assertEquals(bean.getId(), bean1.getId());
    assertEquals(bean.getName(), bean1.getName());
    assertEquals(bean.getContent().get("contentType"), bean1.getContent().get("contentType"));
    assertEquals(18L, bean1.getContent().get("docId"));

    bean1.setName("just change name");
    DB.save(bean1);

    // content changes detected - dirty state so included in update
    Map<String, Object> content1 = bean1.getContent();
    content1.put("additional", "newValue");
    content1.put("docId", 99L);
    bean1.setName("two");
    DB.save(bean1);

    EBasicJsonMap bean2 = DB.find(EBasicJsonMap.class, bean.getId());

    // name changed and docId changed
    assertEquals("two", bean2.getName());
    assertEquals(99L, bean2.getContent().get("docId"));
    assertEquals("newValue", bean2.getContent().get("additional"));

    content1.put("additional", "modValue");
    bean1.setName("three");
    bean1.setContent(content1);
    DB.save(bean1);

    EBasicJsonMap bean3 = DB.find(EBasicJsonMap.class, bean.getId());

    assertEquals("three", bean3.getName());
    assertEquals(99L, bean3.getContent().get("docId"));
    assertEquals("modValue", bean3.getContent().get("additional"));
  }

  @Test
  public void updateQuery_bindingMap() throws IOException {

    String s0 = "{\"docId\":22,\"contentId\":\"initialDoc\"}";
    Map<String, Object> content = EJson.parseObject(s0);

    bean.setName("one");
    bean.setContent(content);

    DB.save(bean);

    String s1 = "{\"docId\":222,\"contentId\":\"updatedDoc222\"}";
    Map<String, Object> content1 = EJson.parseObject(s1);

    LoggedSql.start();

    final int rows = DB.update(EBasicJsonMap.class)
      .set("content", content1)
      .where().eq("id", bean.getId())
      .update();

    final List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update ebasic_json_map set content=? where id = ?");
    assertThat(rows).isEqualTo(1);

    final EBasicJsonMap found = DB.find(EBasicJsonMap.class, bean.getId());
    final Map<String, Object> content2 = found.getContent();
    assertThat(content2.get("contentId")).isEqualTo("updatedDoc222");
    assertThat(content2.get("docId")).isEqualTo(222L);

  }
}

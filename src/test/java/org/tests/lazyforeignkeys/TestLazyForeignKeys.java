package org.tests.lazyforeignkeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.List;

import org.ebeantest.LoggedSqlCollector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.text.PathProperties;

public class TestLazyForeignKeys extends BaseTestCase {

  @Before
  public void prepare() {
    MainEntity ent1 = new MainEntity();
    ent1.setId("ent1");
    ent1.setAttr1("attr1");
    DB.save(ent1);

    MainEntityRelation rel1 = new MainEntityRelation();
    rel1.setId1("ent1");
    rel1.setId2("ent2");
    DB.save(rel1);
  }
  @After
  public void cleanup() {
    DB.find(MainEntity.class).delete();
    DB.find(MainEntityRelation.class).delete();
  }

  @Test
  public void testFindOne() throws Exception {
    // use findOne without select, so lazy loading will occur
    LoggedSqlCollector.start();

    ViewMainEntityRelation vwRel1 = DB.find(ViewMainEntityRelation.class).findOne();

    assertEquals("ent1", vwRel1.getEntity1().getId());
    assertEquals("ent2", vwRel1.getEntity2().getId());

    assertEquals("attr1", vwRel1.getEntity1().getAttr1());
    assertFalse(vwRel1.getEntity1().isDeleted());
    assertTrue(vwRel1.getEntity2().isDeleted());

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(3);
    assertThat(loggedSql.get(0)).contains("select t0.id, t0.attr1, t0.id1, t0.id2 from vw_main_entity_relation");
    assertThat(loggedSql.get(1)).contains("select t0.id, t0.attr1, t0.attr2, t0.id is null from main_entity t0");
    assertThat(loggedSql.get(2)).contains("select t0.id, t0.attr1, t0.attr2, t0.id is null from main_entity t0");
  }
  @Test
  public void testFindListWithSelect() {
    PathProperties pathProp = new PathProperties();
    pathProp.addToPath(null, "attr1");
    pathProp.addToPath("entity1", "id");
    pathProp.addToPath("entity2", "id");

    Query<ViewMainEntityRelation> query = Ebean.find(ViewMainEntityRelation.class).apply(pathProp);
    List<ViewMainEntityRelation> list = query.findList();
    assertEquals(1, list.size());

    System.out.println(query.getGeneratedSql());
    assertThat(query.getGeneratedSql()).contains("t0.id, t0.attr1, t0.id1, t0.id2, t1.id, t2.id");

    ViewMainEntityRelation vwRel1 = list.get(0);
    assertEquals("ent1", vwRel1.getEntity1().getId());
    assertEquals("ent2", vwRel1.getEntity2().getId());

    assertEquals("attr1", vwRel1.getEntity1().getAttr1());
    assertFalse(vwRel1.getEntity1().isDeleted());
    assertTrue(vwRel1.getEntity2().isDeleted());

  }
}

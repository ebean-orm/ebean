package org.tests.lazyforeignkeys;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import io.ebean.text.PathProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Cat;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

public class TestLazyForeignKeys extends BaseTestCase {

  @BeforeEach
  public void prepare() {
    MainEntity ent1 = new MainEntity();
    ent1.setId("ent1");
    ent1.setAttr1("attr1");
    DB.save(ent1);

    MainEntityRelation rel1 = new MainEntityRelation();
    MainEntity e1 = new MainEntity();
    e1.setId("ent1");
    MainEntity e2 = new MainEntity();
    e2.setId("ent2");

    rel1.setEntity1(e1);
    rel1.setEntity2(e2);

    Cat cat = new Cat();
    cat.setId(4711L);
    rel1.setCat2(cat);
    DB.save(rel1);
  }

  @AfterEach
  public void cleanup() {
    DB.find(MainEntity.class).delete();
    DB.find(MainEntityRelation.class).delete();
  }

  @Test
  public void testFindOne() throws Exception {
    // use findOne without select, so lazy loading will occur
    LoggedSql.start();

    MainEntityRelation rel1 = DB.find(MainEntityRelation.class).findOne();

    assertEquals("ent1", rel1.getEntity1().getId());
    assertEquals("ent2", rel1.getEntity2().getId());

    assertEquals("attr1", rel1.getEntity1().getAttr1());
    assertFalse(rel1.getEntity1().isDeleted());
    assertTrue(rel1.getEntity2().isDeleted());

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(3);
    assertSql(sql.get(0)).contains("select t0.id, t0.attr1, t0.id1, t0.id2, t1.species, t0.cat_id, t2.species, t0.cat2_id "
        + "from main_entity_relation t0 left join animal t1 on t1.id = t0.cat_id left join animal t2 on t2.id = t0.cat2_id");
    if (isSqlServer() || isOracle()) {
      assertSql(sql.get(1)).contains("select t0.id, t0.attr1, t0.attr2, CASE WHEN t0.id is null THEN 1 ELSE 0 END from main_entity t0");
    } else {
      assertSql(sql.get(1)).contains("select t0.id, t0.attr1, t0.attr2, t0.id is null from main_entity t0");
      assertSql(sql.get(2)).contains("select t0.id, t0.attr1, t0.attr2, t0.id is null from main_entity t0");
    }

    assertThat(rel1.getCat2().getId()).isEqualTo(4711L);
    assertThatThrownBy(() -> rel1.getCat2().getName()).isInstanceOf(EntityNotFoundException.class);

    Cat cat = new Cat();
    cat.setId(4711L);
    cat.setName("miau");
    DB.save(cat);

    DB.refresh(rel1);
    assertThat(rel1.getCat2().getName()).isEqualTo("miau");
  }

  @Test
  public void testFindListWithSelect() {
    PathProperties pathProp = new PathProperties();
    pathProp.addToPath(null, "attr1");
    pathProp.addToPath("entity1", "id");
    pathProp.addToPath("entity2", "id");

    Query<MainEntityRelation> query = DB.find(MainEntityRelation.class).apply(pathProp);
    List<MainEntityRelation> list = query.findList();
    assertEquals(1, list.size());

    assertSql(query).contains("t0.id, t0.attr1, t0.id1, t0.id2, t1.id, t2.id");

    MainEntityRelation rel1 = list.get(0);
    assertEquals("ent1", rel1.getEntity1().getId());
    assertEquals("ent2", rel1.getEntity2().getId());

    assertEquals("attr1", rel1.getEntity1().getAttr1());
    assertFalse(rel1.getEntity1().isDeleted());
    assertTrue(rel1.getEntity2().isDeleted());
  }

  @Test
  public void testGetWithDbForeignKey() {
    MainEntityRelation relation = DB.find(MainEntityRelation.class).findOne();
    Cat cat = new Cat();
    cat.setId(123L);
    relation.setCat(cat);
    DB.save(relation);

    relation = DB.find(MainEntityRelation.class).findOne();

    assertNotNull(relation.getCat());
  }
}

package org.tests.lazyforeignkeys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.text.PathProperties;

public class TestLazyForeignKeys extends BaseTestCase {

  @Test
  public void test() {
    MainEntity ent1 = new MainEntity();
    ent1.setId("ent1");
    ent1.setAttr1("attr1");
    DB.save(ent1);
    
    MainEntityRelation rel1 = new MainEntityRelation();
    rel1.setId1("ent1");
    rel1.setId2("ent2");
    DB.save(rel1);
    
    ViewMainEntityRelation vwRel1 = DB.find(ViewMainEntityRelation.class).findOne();
    
    assertEquals("ent1", vwRel1.getEntity1().getId());
    assertEquals("ent2", vwRel1.getEntity2().getId());
    
    assertEquals("attr1", vwRel1.getEntity1().getAttr1());
    //assertNull(vwRel1.getEntity2().getAttr1());
    
    PathProperties pathProp = new PathProperties();
    pathProp.addToPath(null, "attr1");
    pathProp.addToPath("entity1", "id");
    pathProp.addToPath("entity2", "id");
    
    Query<ViewMainEntityRelation> query = Ebean.find(ViewMainEntityRelation.class).apply(pathProp);
    List<ViewMainEntityRelation> list = query.findList();
    assertEquals(1, list.size());
    
    assertEquals("ent1", list.get(0).getEntity1().getId());
    assertEquals("ent2", list.get(0).getEntity2().getId());
    
    assertEquals("select t0.id, t0.attr1, t0.id1, t0.id2 from vw_main_entity_relation t0",
        query.getGeneratedSql());
  }
}

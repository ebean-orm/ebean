package org.tests.compositekeys;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;
import org.tests.model.basic.CKeyParent;
import org.tests.model.basic.CKeyParentId;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestCKeyDelete extends BaseTestCase {

  @Test
  public void test() {

    CKeyParentId id = new CKeyParentId(100, "deleteMe");
    CKeyParentId searchId = new CKeyParentId(100, "deleteMe");

    CKeyParent p = new CKeyParent();
    p.setId(id);
    p.setName("testDelete");

    DB.save(p);

    CKeyParent found = DB.find(CKeyParent.class).where().idEq(searchId).findOne();
    assertNotNull(found);

    DB.delete(CKeyParent.class, searchId);

    CKeyParent notFound = DB.find(CKeyParent.class).where().idEq(searchId).findOne();
    assertNull(notFound);

  }

  @Test
  public void testDeleteWhere() {

    CKeyParentId id = new CKeyParentId(101, "deleteMe2");
    CKeyParentId searchId = new CKeyParentId(101, "deleteMe2");

    CKeyParent p = new CKeyParent();
    p.setId(id);
    p.setName("testDelete");

    DB.save(p);

    List<CKeyParentId> ids = DB.find(CKeyParent.class).where().eq("id.oneKey", 101).findIds();
    assertThat(ids).hasSize(1);

    CKeyParentId foundId = ids.get(0);
    assertThat(foundId.getOneKey()).isEqualTo(101);
    assertThat(foundId.getTwoKey()).isEqualTo("deleteMe2");

    DB.createQuery(CKeyParent.class).where().eq("id.oneKey", 101).delete();

    CKeyParent found = DB.find(CKeyParent.class).where().idEq(searchId).findOne();
    assertNull(found);
  }
}

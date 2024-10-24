package org.tests.compositekeys;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.PagedList;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.CKeyAssoc;
import org.tests.model.basic.CKeyDetail;
import org.tests.model.basic.CKeyParent;
import org.tests.model.basic.CKeyParentId;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestCKeyLazyLoad extends BaseTestCase {

  @Test
  public void test() {

    DB.find(CKeyDetail.class).delete();
    DB.find(CKeyParent.class).delete();
    DB.find(CKeyAssoc.class).delete();

    CKeyParentId id = new CKeyParentId(1, "one");

    CKeyAssoc assoc = new CKeyAssoc();
    assoc.setAssocOne("assocOne");

    CKeyParent p = new CKeyParent();
    p.setId(id);
    p.setName("testone");
    p.setAssoc(assoc);
    p.add(new CKeyDetail("somethine one"));
    p.add(new CKeyDetail("somethine two"));

    DB.save(p);

    CKeyAssoc assoc2 = new CKeyAssoc();
    assoc2.setAssocOne("assocTwo");

    CKeyParentId id2 = new CKeyParentId(2, "two");

    CKeyParent p2 = new CKeyParent();
    p2.setId(id2);
    p2.setName("testone");
    p2.setAssoc(assoc2);
    p2.add(new CKeyDetail("somethine one"));
    p2.add(new CKeyDetail("somethine two"));

    DB.save(p2);

    exerciseMaxRowsQuery_with_embeddedId();

    CKeyParentId searchId = new CKeyParentId(1, "one");

    CKeyParent found = DB.find(CKeyParent.class).where().idEq(searchId).findOne();

    assertNotNull(found);
    assertEquals(2, found.getDetails().size());

    List<CKeyParent> list = DB.find(CKeyParent.class).findList();

    assertThat(list.size()).isGreaterThan(1);

    CKeyParent foundFirst = list.get(0);
    List<CKeyDetail> details = foundFirst.getDetails();

    int size = details.size();
    assertThat(size).isGreaterThan(0);

    List<Object> idList = new ArrayList<>();
    idList.add(id);
    idList.add(id2);

    Query<CKeyParent> queryIdIn = DB.find(CKeyParent.class).where().idIn(idList).query();

    List<CKeyParent> idInTestList = queryIdIn.findList();

    assertThat(idInTestList.size()).isEqualTo(2);

    List<CKeyParent> idInTestList2 = DB.find(CKeyParent.class).where().idIn(id, id2).findList();
    assertThat(idInTestList2).hasSameSizeAs(idInTestList);
  }

  /**
   * Exercise paging/maxRows type query with EmbeddedId.
   */
  private void exerciseMaxRowsQuery_with_embeddedId() {

    PagedList<CKeyParent> siteUserPage = DB.find(CKeyParent.class).where()
      .orderBy("name asc")
      .setMaxRows(10)
      .findPagedList();
    siteUserPage.getList();
  }
}

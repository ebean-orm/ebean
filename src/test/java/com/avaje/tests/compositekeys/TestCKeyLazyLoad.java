package com.avaje.tests.compositekeys;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.CKeyAssoc;
import com.avaje.tests.model.basic.CKeyDetail;
import com.avaje.tests.model.basic.CKeyParent;
import com.avaje.tests.model.basic.CKeyParentId;

public class TestCKeyLazyLoad extends BaseTestCase {

  @Test
  public void test() {

    CKeyAssoc assoc = new CKeyAssoc();
    assoc.setAssocOne("assocOne");

    CKeyParentId id = new CKeyParentId(1, "one");

    CKeyParent p = new CKeyParent();
    p.setId(id);
    p.setName("testone");
    p.setAssoc(assoc);
    p.add(new CKeyDetail("somethine one"));
    p.add(new CKeyDetail("somethine two"));

    Ebean.save(p);

    CKeyAssoc assoc2 = new CKeyAssoc();
    assoc2.setAssocOne("assocTwo");

    CKeyParentId id2 = new CKeyParentId(2, "two");

    CKeyParent p2 = new CKeyParent();
    p2.setId(id2);
    p2.setName("testone");
    p2.setAssoc(assoc2);
    p2.add(new CKeyDetail("somethine one"));
    p2.add(new CKeyDetail("somethine two"));

    Ebean.save(p2);

    CKeyParentId searchId = new CKeyParentId(1, "one");

    CKeyParent found = Ebean.find(CKeyParent.class).where().idEq(searchId).findUnique();

    Assert.assertNotNull(found);
    Assert.assertEquals(2,found.getDetails().size());

    List<CKeyParent> list = Ebean.find(CKeyParent.class).findList();

    Assert.assertTrue(list.size() > 1);

    CKeyParent foundFirst = list.get(0);
    List<CKeyDetail> details = foundFirst.getDetails();

    int size = details.size();
    Assert.assertTrue(size > 0);

    List<Object> idList = new ArrayList<Object>();
    idList.add(id);
    idList.add(id2);

    Query<CKeyParent> queryIdIn = Ebean.find(CKeyParent.class).where().idIn(idList).query();

    List<CKeyParent> idInTestList = queryIdIn.findList();

    Assert.assertTrue(idInTestList.size() == 2);

  }
}

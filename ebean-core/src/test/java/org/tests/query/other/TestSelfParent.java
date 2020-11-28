package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.selfref.SelfParent;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestSelfParent extends BaseTestCase {

  @Test
  public void test() {

    if (Ebean.find(SelfParent.class).findCount() > 0) {
      // only run once
      return;
    }

    SelfParent root = new SelfParent("root", null);
    SelfParent child1 = new SelfParent("child1", root);
    SelfParent child11 = new SelfParent("child11", child1);
    SelfParent child111 = new SelfParent("child111", child11);
    SelfParent child12 = new SelfParent("child12", child1);

    SelfParent child2 = new SelfParent("child2", root);
    SelfParent child21 = new SelfParent("child21", child2);
    SelfParent child22 = new SelfParent("child22", child2);

    Ebean.save(root);
    Ebean.save(child1);
    Ebean.save(child11);
    Ebean.save(child111);
    Ebean.save(child12);
    Ebean.save(child2);
    Ebean.save(child21);
    Ebean.save(child22);

    List<SelfParent> roots = Ebean.find(SelfParent.class).where().eq("parent", null).findList();

    Assert.assertEquals(1, roots.size());

    printNode(roots.get(0));

  }

  public static void printNode(SelfParent o) {
    //System.out.println(o.getName());
    for (SelfParent c : o.getChildren()) {
      printNode(c);
    }
  }

}

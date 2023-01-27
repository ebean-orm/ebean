package org.tests.query.other;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.selfref.SelfParent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSelfParent extends BaseTestCase {

  @Test
  public void test() {

    if (DB.find(SelfParent.class).findCount() > 0) {
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

    DB.save(root);
    DB.save(child1);
    DB.save(child11);
    DB.save(child111);
    DB.save(child12);
    DB.save(child2);
    DB.save(child21);
    DB.save(child22);

    List<SelfParent> roots = DB.find(SelfParent.class).where().eq("parent", (String) null).findList();

    assertEquals(1, roots.size());

    printNode(roots.get(0));
  }

  public static void printNode(SelfParent o) {
    //System.out.println(o.getName());
    for (SelfParent c : o.getChildren()) {
      printNode(c);
    }
  }

}

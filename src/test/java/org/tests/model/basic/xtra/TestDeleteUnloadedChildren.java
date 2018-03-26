package org.tests.model.basic.xtra;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import org.junit.Test;

import java.util.ArrayList;

public class TestDeleteUnloadedChildren extends BaseTestCase {

  private void init() {

    try (Transaction txn = Ebean.beginTransaction()) {
      String sql;
      SqlUpdate delete;

      sql = "delete from td_child";
      delete = Ebean.createSqlUpdate(sql);
      delete.execute();

      sql = "delete from td_parent";
      delete = Ebean.createSqlUpdate(sql);
      delete.execute();

      EdParent parent = new EdParent();
      parent.setName("MyComputer");

      EdChild child = new EdChild();
      child.setName("Harddisk 123");
      child.setParent(parent);
      ArrayList<EdChild> children = new ArrayList<>();
      children.add(child);
      parent.setChildren(children);

      Ebean.save(parent);

      EdExtendedParent extendedParent = new EdExtendedParent();
      extendedParent.setName("My second computer");
      extendedParent.setExtendedName("Multimedia");

      child = new EdChild();
      child.setName("DVBS Card");
      children = new ArrayList<>();
      children.add(child);
      extendedParent.setChildren(children);

      Ebean.save(extendedParent);

      txn.commit();
    }
  }

  @Test
  public void testCascadeDelete2() {

    init();

    try (Transaction txn = Ebean.beginTransaction()) {
      EdParent parent = Ebean.find(EdParent.class).where().eq("name", "MyComputer").findOne();
      // // Works only if the following statement is included
      // int x = parent.getChildren().size();
      Ebean.delete(parent);
      txn.commit();
    }
  }

  @Test
  public void testCascadeDelete3() {

    init();

    Ebean.beginTransaction();
    try {
      EdExtendedParent extendedParent = Ebean.find(EdExtendedParent.class).where()
        .eq("name", "My second computer").findOne();
      extendedParent.getChildren().size();
      Ebean.delete(extendedParent);
      Ebean.commitTransaction();
    } finally {
      Ebean.endTransaction();
    }
  }

}

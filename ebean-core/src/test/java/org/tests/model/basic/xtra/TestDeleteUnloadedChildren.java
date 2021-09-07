package org.tests.model.basic.xtra;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class TestDeleteUnloadedChildren extends BaseTestCase {

  private void init() {

    try (Transaction txn = DB.beginTransaction()) {
      String sql;
      SqlUpdate delete;

      sql = "delete from td_child";
      delete = DB.createSqlUpdate(sql);
      delete.execute();

      sql = "delete from td_parent";
      delete = DB.createSqlUpdate(sql);
      delete.execute();

      EdParent parent = new EdParent();
      parent.setName("MyComputer");

      EdChild child = new EdChild();
      child.setName("Harddisk 123");
      child.setParent(parent);
      ArrayList<EdChild> children = new ArrayList<>();
      children.add(child);
      parent.setChildren(children);

      DB.save(parent);

      EdExtendedParent extendedParent = new EdExtendedParent();
      extendedParent.setName("My second computer");
      extendedParent.setExtendedName("Multimedia");

      child = new EdChild();
      child.setName("DVBS Card");
      children = new ArrayList<>();
      children.add(child);
      extendedParent.setChildren(children);

      DB.save(extendedParent);

      txn.commit();
    }
  }

  @Test
  public void testCascadeDelete2() {

    init();

    try (Transaction txn = DB.beginTransaction()) {
      EdParent parent = DB.find(EdParent.class).where().eq("name", "MyComputer").findOne();
      // // Works only if the following statement is included
      // int x = parent.getChildren().size();
      DB.delete(parent);
      txn.commit();
    }
  }

  @Test
  public void testCascadeDelete3() {

    init();

    DB.beginTransaction();
    try {
      EdExtendedParent extendedParent = DB.find(EdExtendedParent.class).where()
        .eq("name", "My second computer").findOne();
      extendedParent.getChildren().size();
      DB.delete(extendedParent);
      DB.commitTransaction();
    } finally {
      DB.endTransaction();
    }
  }

}

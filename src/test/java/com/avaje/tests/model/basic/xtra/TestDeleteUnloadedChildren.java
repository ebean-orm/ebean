package com.avaje.tests.model.basic.xtra;

import java.util.ArrayList;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;

public class TestDeleteUnloadedChildren extends BaseTestCase {

  private void init() {

    Ebean.beginTransaction();
    try {
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
      ArrayList<EdChild> children = new ArrayList<EdChild>();
      children.add(child);
      parent.setChildren(children);

      Ebean.save(parent);

      EdExtendedParent extendedParent = new EdExtendedParent();
      extendedParent.setName("My second computer");
      extendedParent.setExtendedName("Multimedia");

      child = new EdChild();
      child.setName("DVBS Card");
      children = new ArrayList<EdChild>();
      children.add(child);
      extendedParent.setChildren(children);

      Ebean.save(extendedParent);

      Ebean.commitTransaction();
    } finally {
      Ebean.endTransaction();
    }
  }

  @Test
  public void testCascadeDelete2() {

    init();

    Ebean.beginTransaction();
    try {
      EdParent parent = Ebean.find(EdParent.class).where().eq("name", "MyComputer").findUnique();
      // // Works only if the following statement is included
      // int x = parent.getChildren().size();
      Ebean.delete(parent);
      Ebean.commitTransaction();
    } finally {
      Ebean.endTransaction();
    }
  }

  @Test
  public void testCascadeDelete3() {

    init();

    Ebean.beginTransaction();
    try {
      EdExtendedParent extendedParent = Ebean.find(EdExtendedParent.class).where()
          .eq("name", "My second computer").findUnique();
      extendedParent.getChildren().size();
      Ebean.delete(extendedParent);
      Ebean.commitTransaction();
    } finally {
      Ebean.endTransaction();
    }
  }

}

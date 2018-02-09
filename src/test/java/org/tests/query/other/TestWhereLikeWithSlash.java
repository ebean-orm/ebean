package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.junit.Test;
import org.tests.model.basic.EBasic;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestWhereLikeWithSlash extends BaseTestCase {

  @Test
  public void testWithSlash() {

    EBasic basic = new EBasic("slash\\monkey");
    Ebean.save(basic);

    Query<EBasic> query = Ebean.find(EBasic.class).where().startsWith("name", "slash\\mon").query();
    List<EBasic> list = query.findList();

    assertEquals(1, list.size());

    Query<EBasic> query1 = Ebean.find(EBasic.class).where().like("name", "slash\\mon%").query();
    List<EBasic> list1 = query1.findList();

    // This doesn't work in the latest version of H2 so disable for now.
    // Still good on Postgres which was the original issue
    assertEquals(1, list1.size());

  }

  @Test
  public void testWithPipe() {

    EBasic basic = new EBasic("bash|monkey");
    Ebean.save(basic);

    Query<EBasic> query = Ebean.find(EBasic.class).where().startsWith("name", "bash|mo").query();
    List<EBasic> list = query.findList();

    assertEquals(1, list.size());

    Query<EBasic> query1 = Ebean.find(EBasic.class).where().like("name", "bash|mon%").query();
    List<EBasic> list1 = query1.findList();

    // This doesn't work in the latest version of H2 so disable for now.
    // Still good on Postgres which was the original issue
    assertEquals(1, list1.size());
  }

  @Test
  public void testWithOpenSquare() {

    EBasic basic = new EBasic("mash[monkey");
    Ebean.save(basic);

    Query<EBasic> query = Ebean.find(EBasic.class).where().startsWith("name", "mash[mo").query();
    List<EBasic> list = query.findList();

    assertEquals(1, list.size());

    List<EBasic> list1;
    if (isSqlServer()) {
      list1 = Ebean.find(EBasic.class).where().like("name", "mash[[]mon%").query().findList();

    } else {
      list1 = Ebean.find(EBasic.class).where().like("name", "mash[mon%").query().findList();
    }

    // This doesn't work in the latest version of H2 so disable for now.
    // Still good on Postgres which was the original issue
    assertEquals(1, list1.size());
  }
}

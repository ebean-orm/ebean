package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestWhereLikeWithSlash extends BaseTestCase {

  @Test
  public void testWithSlash() {

    EBasic basic = new EBasic("slash\\monkey");
    DB.save(basic);

    Query<EBasic> query = DB.find(EBasic.class).where().startsWith("name", "slash\\mon").query();
    List<EBasic> list = query.findList();

    assertEquals(1, list.size());

    Query<EBasic> query1 = DB.find(EBasic.class).where().like("name", "slash\\mon%").query();
    List<EBasic> list1 = query1.findList();

    if (!isMySql() && !isNuoDb() && !isMariaDB()) {
      // For mysql this assert depends on no_backslash_escapes setting so we won't assert here
      // Still good on Postgres which was the original issue
      assertEquals(1, list1.size());
    }
  }

  @Test
  public void testWithPipe() {

    EBasic basic = new EBasic("bash|monkey");
    DB.save(basic);

    Query<EBasic> query = DB.find(EBasic.class).where().startsWith("name", "bash|mo").query();
    List<EBasic> list = query.findList();

    assertEquals(1, list.size());

    Query<EBasic> query1 = DB.find(EBasic.class).where().like("name", "bash|mon%").query();
    List<EBasic> list1 = query1.findList();

    // This doesn't work in the latest version of H2 so disable for now.
    // Still good on Postgres which was the original issue
    assertEquals(1, list1.size());
  }

  @Test
  public void testWithOpenSquare() {

    EBasic basic = new EBasic("mash[monkey");
    DB.save(basic);

    Query<EBasic> query = DB.find(EBasic.class).where().startsWith("name", "mash[mo").query();
    List<EBasic> list = query.findList();

    assertEquals(1, list.size());

    List<EBasic> list1;
    if (isSqlServer()) {
      list1 = DB.find(EBasic.class).where().like("name", "mash[[]mon%").query().findList();

    } else {
      list1 = DB.find(EBasic.class).where().like("name", "mash[mon%").query().findList();
    }

    // This doesn't work in the latest version of H2 so disable for now.
    // Still good on Postgres which was the original issue
    assertEquals(1, list1.size());
  }
}

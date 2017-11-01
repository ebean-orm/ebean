package org.tests.softdelete;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.annotation.PersistBatch;
import org.tests.model.softdelete.ESoftDelMid;
import org.tests.model.softdelete.ESoftDelTop;
import org.tests.model.softdelete.ESoftDelUp;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSoftDeleteTop extends BaseTestCase {

  @Test
  public void testDeletePermanent() {

    ESoftDelUp up1 = new ESoftDelUp("up1");
    Ebean.save(up1);

    Ebean.delete(up1);
    Ebean.deletePermanent(up1);
  }


  @Test
  public void testSoftDeleteJdbcBatch() {

    ESoftDelUp up1 = new ESoftDelUp("upBatch1");
    Ebean.save(up1);
    ESoftDelUp up2 = new ESoftDelUp("upBatch2");
    Ebean.save(up2);

    Transaction transaction = Ebean.beginTransaction();
    try {
      transaction.setBatch(PersistBatch.ALL);
      Ebean.delete(up1);
      Ebean.delete(up2);
      transaction.commit();
    } finally {
      transaction.end();
    }

    List<ESoftDelUp> list = new ArrayList<>();
    list.add(up1);
    list.add(up2);

    Ebean.deleteAllPermanent(list);
  }

  @Test
  public void testSoftDeleteAll() {

    ESoftDelUp up1 = new ESoftDelUp("upBatchX");
    ESoftDelUp up2 = new ESoftDelUp("upBatchY");

    List<ESoftDelUp> list = new ArrayList<>();
    list.add(up1);
    list.add(up2);

    // by default uses JDBC for the 'all' methods
    Ebean.saveAll(list);
    Ebean.deleteAll(list);
    Ebean.deleteAllPermanent(list);

  }

  @Test
  public void test() {

    ESoftDelUp up1 = new ESoftDelUp("up1");

    ESoftDelTop top1 = new ESoftDelTop("top1");
    ESoftDelMid mid1 = top1.addMids("mid1");
    mid1.addDown("down1");
    mid1.addDown("down2");
    mid1.setUp(up1);

    ESoftDelMid mid2 = top1.addMids("mid2");
    mid2.addDown("down3");
    mid2.addDown("down4");

    Ebean.save(top1);

    Ebean.delete(top1);

    Ebean.deletePermanent(top1);
  }

  @Test
  public void testWhereNull() {

    ESoftDelTop top1 = new ESoftDelTop("top1");
    top1.addMids("mid1");
    top1.addMids("mid2");

    Ebean.save(top1);

    Query<ESoftDelTop> query = Ebean.find(ESoftDelTop.class)
      .where().isEmpty("mids")
      .query();

    query.findList();

    assertThat(sqlOf(query)).contains("where not exists (select 1 from esoft_del_mid x where x.top_id = t0.id and x.deleted =");
  }

}

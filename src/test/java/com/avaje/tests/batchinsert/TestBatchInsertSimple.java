package com.avaje.tests.batchinsert;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.tests.model.basic.UTDetail;
import com.avaje.tests.model.basic.UTMaster;

public class TestBatchInsertSimple extends BaseTestCase {

  Random random = new Random();

  @Test
  public void testSimpleJdbcBatching() {

    int numOfMasters = 10;// 2 + random.nextInt(8);

    List<UTMaster> masters = new ArrayList<UTMaster>();
    for (int i = 0; i < numOfMasters; i++) {
      masters.add(createMasterAndDetails(i));
    }

    Transaction transaction = Ebean.beginTransaction();
    try {
      transaction.setBatchMode(true);
      transaction.setBatchSize(4);
      // transaction.setLogLevel(LogLevel.SUMMARY);
      // transaction.setBatchGetGeneratedKeys(false);

      Ebean.save(masters);

      transaction.commit();

    } finally {
      Ebean.endTransaction();
    }
  }

  private UTMaster createMasterAndDetails(int masterPos) {

    UTMaster master = createMaster(masterPos);
    List<UTDetail> details = new ArrayList<UTDetail>();

    int count = 2 + random.nextInt(20);

    for (int i = 0; i < count; i++) {

      int qty = 1 + random.nextInt(99);
      double amount = random.nextDouble();

      details.add(createDetail(masterPos + "-" + i, qty, amount));
    }
    master.setDetails(details);
    return master;
  }

  private UTMaster createMaster(int position) {
    UTMaster m = new UTMaster();
    m.setName("batchInsert-master" + position);
    return m;
  }

  private UTDetail createDetail(String position, int qty, double amount) {

    UTDetail detail = new UTDetail();
    detail.setName("batchInsert-detail-" + position);
    detail.setQty(Integer.valueOf(qty));
    detail.setAmount(Double.valueOf(amount));

    // System.out.println("-- "+detail);

    return detail;
  }

}

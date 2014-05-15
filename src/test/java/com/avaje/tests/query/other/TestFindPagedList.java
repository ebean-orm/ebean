package com.avaje.tests.query.other;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Transaction;
import com.avaje.tests.model.basic.EBasic;

public class TestFindPagedList extends BaseTestCase {

  @Test
  public void test() throws InterruptedException, ExecutionException {
    

    EbeanServer server = Ebean.getServer(null);

    Transaction transaction = server.beginTransaction();
    try {
      transaction.setBatchMode(true);
      transaction.setBatchSize(20);
      for (int i = 0; i < 87; i++) {
        EBasic dumbModel = new EBasic();
        dumbModel.setName("HelloB0Bi");
        server.save(dumbModel);
      }
      transaction.commit();

    } finally {
      transaction.end();
    }    
    
    PagedList<EBasic> list1 = Ebean.find(EBasic.class)
          .where().like("name", "HelloB0B%")
          .findPagedList(0, 10);
    
    list1.loadRowCount();
    List<EBasic> list = list1.getList();
    int totalRowCount = list1.getTotalRowCount();
    int totalPageCount = list1.getTotalPageCount();
    
    Assert.assertEquals(10, list.size());
    Assert.assertEquals(87, totalRowCount);
    Assert.assertEquals(9, totalPageCount);
    
    PagedList<EBasic> list2 = Ebean.find(EBasic.class)
        .where().like("name", "HelloB0B%")
        .findPagedList(4, 10);
  
    list = list2.getList();
    
    Assert.assertEquals(10, list2.getList().size());
    Assert.assertEquals(87, list2.getTotalRowCount());
    Assert.assertEquals(9, list2.getTotalPageCount());
    
    PagedList<EBasic> list3 = Ebean.find(EBasic.class)
        .where().like("name", "HelloB0B%")
        .findPagedList(8, 10);
  
    Future<Integer> rowCount = list3.getFutureRowCount();
    list = list3.getList();
    
    Assert.assertEquals(Integer.valueOf(87), rowCount.get());
    Assert.assertEquals(7, list3.getList().size());
    Assert.assertEquals(87, list3.getTotalRowCount());
    Assert.assertEquals(9, list3.getTotalPageCount());
  }
  
}

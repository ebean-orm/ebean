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
    
    PagedList<EBasic> page1 = Ebean.find(EBasic.class)
          .where().like("name", "HelloB0B%")
          .findPagedList(0, 10);
    
    page1.loadRowCount();
    List<EBasic> list = page1.getList();
    
    Assert.assertEquals(10, list.size());
    Assert.assertEquals(87, page1.getTotalRowCount());
    Assert.assertEquals(9, page1.getTotalPageCount());
    Assert.assertEquals(true, page1.hasNext());
    
    
    PagedList<EBasic> page1b = Ebean.find(EBasic.class)
        .where().like("name", "HelloB0B%")
        .findPagedList(0, 10);
  
    // Same as page1 but without initial loadRowCount() call
    List<EBasic> list1B = page1b.getList();
    Assert.assertEquals(10, list1B.size());
    Assert.assertEquals(87, page1b.getTotalRowCount());
    Assert.assertEquals(9, page1b.getTotalPageCount());
    Assert.assertEquals(true, page1.hasNext());
  
    
    PagedList<EBasic> page2 = Ebean.find(EBasic.class)
        .where().like("name", "HelloB0B%")
        .findPagedList(4, 10);
  
    list = page2.getList();
    
    Assert.assertEquals(10, page2.getList().size());
    Assert.assertEquals(87, page2.getTotalRowCount());
    Assert.assertEquals(9, page2.getTotalPageCount());
    Assert.assertEquals(true, page2.hasNext());
    
    PagedList<EBasic> page3 = Ebean.find(EBasic.class)
        .where().like("name", "HelloB0B%")
        .findPagedList(8, 10);
  
    Future<Integer> rowCount = page3.getFutureRowCount();
    list = page3.getList();
    
    Assert.assertEquals(Integer.valueOf(87), rowCount.get());
    Assert.assertEquals(7, page3.getList().size());
    Assert.assertEquals(87, page3.getTotalRowCount());
    Assert.assertEquals(9, page3.getTotalPageCount());
    Assert.assertEquals(false, page3.hasNext());
    
  }
  
}

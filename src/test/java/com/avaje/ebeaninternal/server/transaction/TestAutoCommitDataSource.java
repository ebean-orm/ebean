package com.avaje.ebeaninternal.server.transaction;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.tests.model.basic.UTDetail;

public class TestAutoCommitDataSource extends BaseTestCase {

  @Test
  public void test() {
    
    ServerConfig config = new ServerConfig();
    config.setName("h2autocommit");
    config.loadFromProperties();
    
    config.addClass(UTDetail.class);
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setAutoCommitMode(true);
 
    GlobalProperties.setSkipPrimaryServer(true);
    
    EbeanServer ebeanServer = EbeanServerFactory.create(config);
    
    
    UTDetail detail1 = new UTDetail("one", 12, 30D);
    UTDetail detail2 = new UTDetail("two", 11, 30D);
    UTDetail detail3 = new UTDetail("three", 8, 30D);
    
    Transaction txn = ebeanServer.beginTransaction();
    try {
      txn.setBatchMode(true);
      ebeanServer.save(detail1);
      ebeanServer.save(detail2);
      ebeanServer.save(detail3);
      txn.commit();
      
    } finally {
      txn.end();
    }
    
    List<UTDetail> details = ebeanServer.find(UTDetail.class).findList();
    Assert.assertEquals(3, details.size());
    
  }
}

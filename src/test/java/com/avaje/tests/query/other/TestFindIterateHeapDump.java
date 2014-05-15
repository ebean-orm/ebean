package com.avaje.tests.query.other;

import java.io.File;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import org.junit.Ignore;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.Transaction;
import com.avaje.tests.model.basic.EBasic;
import com.sun.management.HotSpotDiagnosticMXBean;

@SuppressWarnings("restriction")
public class TestFindIterateHeapDump extends BaseTestCase {
  
  // All that heap dumping code from :
  // https://blogs.oracle.com/sundararajan/entry/programmatically_dumping_heap_from_java
  private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";

  /**
   * field to store the hotspot diagnostic MBean
   */
  private static volatile HotSpotDiagnosticMXBean hotspotMBean;

  /**
   * Ignore this test generally - run it manually against MySQL specifically for Issue #56.
   */
  @Ignore
  @Test
  public void test() {

    EbeanServer server = Ebean.getServer(null);

    Transaction transaction = server.beginTransaction();
    try {
      transaction.setBatchMode(true);
      transaction.setBatchSize(20);
      for (int i = 0; i < 10000; i++) {
        EBasic dumbModel = new EBasic();
        dumbModel.setName("Hello");
        server.save(dumbModel);
      }
      transaction.commit();

    } finally {
      transaction.end();
    }    

    QueryIterator<EBasic> iterate = server.find(EBasic.class).findIterate();
    iterate.hashCode();
    try {
    
      // Intentionally not iterating through the iterator to 
      
      // try {
      // while (iterate.hasNext()) {
      // EBasic eBasic = iterate.next();
      // eBasic.getDescription();
      // }
      // } finally {
      // iterate.close();
      // }
      
      String fileName = "heap-dump6.snapshot";
      
      File file = new File(fileName);
      if (file.exists())
        file.delete();
  
      dumpHeap(fileName, true);
      
    } finally {
      iterate.close();
    }
    
  }

  static void dumpHeap(String fileName, boolean live) {
    // initialize hotspot diagnostic MBean
    initHotspotMBean();
    try {
      hotspotMBean.dumpHeap(fileName, live);
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception exp) {
      throw new RuntimeException(exp);
    }
  }

  /**
   * initialize the hotspot diagnostic MBean field
   */
  private static void initHotspotMBean() {
    if (hotspotMBean == null) {
      synchronized (TestFindIterateHeapDump.class) {
        if (hotspotMBean == null) {
          hotspotMBean = getHotspotMBean();
        }
      }
    }
  }

  /**
   * get the hotspot diagnostic MBean from the ManagementFactory.
   */
  private static HotSpotDiagnosticMXBean getHotspotMBean() {
    try {
      MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      HotSpotDiagnosticMXBean bean = ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME,
          HotSpotDiagnosticMXBean.class);
      return bean;
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception exp) {
      throw new RuntimeException(exp);
    }
  }

}

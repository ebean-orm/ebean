package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.tests.model.basic.EBasic;
import com.sun.management.HotSpotDiagnosticMXBean;
import org.junit.Ignore;
import org.junit.Test;

import javax.management.MBeanServer;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;

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

    EbeanServer server = Ebean.getDefaultServer();

//    Transaction transaction = server.beginTransaction();
//    try {
//      transaction.setBatchMode(true);
//      transaction.setBatchSize(20);
//      for (int i = 0; i < 20000; i++) {
//        EBasic dumbModel = new EBasic();
//        dumbModel.setName("Goodbye now");
//        server.save(dumbModel);
//      }
//      transaction.commit();
//
//    } finally {
//      transaction.end();
//    }
//
//    if (true) {
//      return;
//    }

    // Intentionally not iterating through the iterator to

    final AtomicInteger counter = new AtomicInteger();

    server.find(EBasic.class)
      .findEach(bean -> {

        int count = counter.incrementAndGet();
        if (count == 1) {
          dumpHeap("heap-dump13-initial.snapshot", true);
        }
      });

    // try {
    // while (iterate.hasNext()) {
    // EBasic eBasic = iterate.next();
    // eBasic.getDescription();
    // }
    // } finally {
    // iterate.close();
    // }

    String fileName = "heap-dump13.snapshot";

    File file = new File(fileName);
    if (file.exists())
      file.delete();

    dumpHeap(fileName, true);
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
      final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      return ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception exp) {
      throw new RuntimeException(exp);
    }
  }

}

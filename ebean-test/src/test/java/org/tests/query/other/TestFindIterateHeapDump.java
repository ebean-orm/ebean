package org.tests.query.other;

import com.sun.management.HotSpotDiagnosticMXBean;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;

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
  @Disabled
  @Test
  void test() {
    Database server = DB.getDefault();

//    String desc = "0123456789".repeat(20);
//    try (Transaction transaction = server.beginTransaction()){
//      transaction.setBatchMode(true);
//      transaction.setBatchSize(100);
//      for (int i = 0; i < 200_000; i++) {
//        EBasic dumbModel = new EBasic();
//        dumbModel.setName("Goodbye now");
//        dumbModel.setDescription(desc);
//        server.save(dumbModel);
//      }
//      transaction.commit();
//    }

    // Intentionally not iterating through the iterator to
    final AtomicInteger counter = new AtomicInteger();

    server.find(EBasic.class)
      // .setBufferFetchSizeHint(10)
      // .findStream().forEach(bean -> {
      .findEach(bean -> {
        int count = counter.incrementAndGet();
        if (count == 10) {
          dumpHeap("dump-initial.snapshot.hprof", true);
        }

        if (count == (200_000 - 100)) {
          dumpHeap("dump-end.snapshot.hprof", true);
        }
      });

    String fileName = "dump.snapshot.hprof";
    File file = new File(fileName);
    if (file.exists()) {
      file.delete();
    }
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

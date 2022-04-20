package io.ebean.bean;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;

import static org.assertj.core.api.Assertions.assertThat;

class NodeUsageCollectorTest {

  private final Listener listener = new Listener();

  /**
   * Run this manually as we make explicit GC call here.
   */
  @Disabled
  @Test
  void test() throws InterruptedException {
    WeakReference<NodeUsageListener> profilingListenerRef = new WeakReference<>(listener);

    ObjectGraphNode node = new ObjectGraphNode((ObjectGraphOrigin)null, "foo");
    NodeUsageCollector c = new NodeUsageCollector(node, profilingListenerRef);
    c.addUsed("a");
    c.addUsed("b");
    c = null;

    System.gc();
    Thread.sleep(100);

    assertThat(listener.collectCount).isEqualTo(1);
  }

  static class Listener implements NodeUsageListener {

    int collectCount;

    @Override
    public void collectNodeUsage(NodeUsageCollector.State collector) {
      collectCount++;
      System.out.println("collectNodeUsage " + collector);
    }
  }
}

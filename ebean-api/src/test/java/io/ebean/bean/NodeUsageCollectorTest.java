package io.ebean.bean;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NodeUsageCollectorTest {

  private final Listener listener = new Listener();

  private final ObjectGraphNode node = new ObjectGraphNode((ObjectGraphOrigin) null, "foo");

  /**
   * Run this manually as we make use of explicit GC call here which is dubious.
   */
  @Disabled
  @Test
  void test() throws InterruptedException {
    NodeUsageCollector c = new NodeUsageCollector(node, listener);
    c.addUsed("a");
    c.addUsed("b");
    c = null;

    System.gc();
    Thread.sleep(100);

    assertThat(listener.collectCount).isEqualTo(1);
    assertThat(node).isNotNull();
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

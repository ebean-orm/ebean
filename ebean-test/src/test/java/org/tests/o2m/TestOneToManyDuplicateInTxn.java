package org.tests.o2m;

import io.ebean.CacheMode;
import io.ebean.DB;
import io.ebean.Transaction;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TestOneToManyDuplicateInTxn {

  @Test
  void findTwice() {
    OMVertex master = new OMVertex(UUID.randomUUID());
    OMVertexOther child = new OMVertexOther("child");
    master.getRelated().add(child);
    DB.save(master);

    try (Transaction txn = DB.beginTransaction()) {
      OMVertex first = DB.find(OMVertex.class)
        .setDisableLazyLoading(true)
        .fetch("related")
        .where().eq("id", master.getId())
        .findOne();

      assertThat(first.getRelated()).hasSize(1);

      OMVertex second = DB.find(OMVertex.class)
        //.setLoadBeanCache(true)
        .setBeanCacheMode(CacheMode.PUT)
        .setDisableLazyLoading(true)
        .fetch("related")
        .where().eq("id", master.getId())
        .findOne();

      assertThat(second.getRelated()).hasSize(1);
    }
  }
}

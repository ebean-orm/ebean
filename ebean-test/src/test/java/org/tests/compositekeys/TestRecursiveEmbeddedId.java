package org.tests.compositekeys;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.compositekeys.db.PartitionKey;
import org.tests.compositekeys.db.EmbeddedSelfRelation;

import static org.assertj.core.api.Assertions.assertThat;

class TestRecursiveEmbeddedId extends BaseTestCase {

  @Test
  void insert() {
    PartitionKey partitionKey = new PartitionKey(78L, 1000L);
    EmbeddedSelfRelation bean0 = new EmbeddedSelfRelation(partitionKey);
    DB.save(bean0);

    var found = DB.find(EmbeddedSelfRelation.class).where().eq("key", new PartitionKey(78L, 1000L)).findOne();
    assertThat(found).isNotNull();
    assertThat(found.key().orgId()).isEqualTo(78L);
    assertThat(found.key().code()).isEqualTo(1000L);
    assertThat(found.root().key()).isNotNull();
    assertThat(found.root().key().orgId()).isEqualTo(78L);
    assertThat(found.root().key().code()).isNull();

    var bean1 = new EmbeddedSelfRelation(new PartitionKey(78L, 1001L));
    bean1.setRoot(bean0);
    DB.save(bean1);

    var found2 = DB.find(EmbeddedSelfRelation.class).where().eq("key", new PartitionKey(78L, 1001L)).findOne();
    assertThat(found2).isNotNull();
    assertThat(found2.key().code()).isEqualTo(1001L);

    assertThat(found2.root().key().orgId()).isEqualTo(78L);
    assertThat(found2.root().key().code()).isEqualTo(1000L);
  }
}

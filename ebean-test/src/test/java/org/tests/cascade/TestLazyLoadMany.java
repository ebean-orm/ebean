package org.tests.cascade;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestLazyLoadMany extends BaseTestCase {

  COOne create(String parentName, int childCount) {
    COOne m0 = new COOne(parentName);
    for (int i = 0; i < childCount; i++) {
      m0.getChildren().add(new COOneMany(parentName+"_"+i));
    }
    return m0;
  }
  @Test
  void loadAfterPCCleared() {

    var m0 = create("tll-m0", 2);
    var m1 = create("tll-m1", 4);
    var m2 = create("tll-m2", 5);

    DB.saveAll(m0, m1, m2);

    try (Transaction transaction = DB.beginTransaction()) {
      List<COOne> children = DB.find(COOne.class)
        .setLazyLoadBatchSize(2)
        .where().startsWith("name", "tll-m")
        .findList();

      assertThat(children).hasSize(3);
      assertThat(children.get(0).getChildren()).describedAs("invoke lazy loading on children").hasSize(2);

      DB.sqlUpdate("update coone set name = ? where id = ?")
        .setParameters("new name", children.get(0).getId())
        .executeNow(); // clears the persistence context in 14.0.0+ due to #3295 #3301

      // the children here were already lazy loaded
      assertThat(children.get(1).getChildren()).hasSize(4);
      // this invokes the lazy loading of children AFTER the persistence context was cleared
      assertThat(children.get(2).getChildren()).hasSize(5);

      transaction.rollback();
    } finally {
      DB.deleteAll(List.of(m0, m1, m2));
    }
  }
}

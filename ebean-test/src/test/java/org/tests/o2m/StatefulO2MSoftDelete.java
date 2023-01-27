package org.tests.o2m;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.o2m.dm.GoodsEntity;
import org.tests.o2m.dm.WorkflowEntity;
import org.tests.o2m.dm.WorkflowOperationEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StatefulO2MSoftDelete extends BaseTestCase {
  @Test
  void statefulUpdateShouldntDelete() {
    var bomGoods = new GoodsEntity();
    DB.save(bomGoods);

    var goods = new GoodsEntity();
    var workflow = new WorkflowEntity();
    var operation1 = new WorkflowOperationEntity();
    operation1.setName("operation 1");
    goods.setWorkflowEntity(workflow);
    workflow.setOperations(List.of(operation1));

    DB.save(goods);

    LoggedSql.start();
    // replace workflow entity
    var goodsFromDB = DB.find(GoodsEntity.class, goods.getId());
    goodsFromDB.setWorkflowEntity(new WorkflowEntity());

    DB.update(goodsFromDB);
    var updateSql = LoggedSql.stop();
    updateSql.forEach(System.out::println);
    updateSql.forEach(sql -> assertThat(sql).doesNotContain("delete from workflow_entity"));
  }
}

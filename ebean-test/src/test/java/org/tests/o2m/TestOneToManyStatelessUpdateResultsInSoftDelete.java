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

public class TestOneToManyStatelessUpdateResultsInSoftDelete extends BaseTestCase {
  @Test
  public void testStatelessUpdate() {
    LoggedSql.start();
    var goods = new GoodsEntity();
    var workflow = new WorkflowEntity();
    var operation1 = new WorkflowOperationEntity();
    goods.setWorkflowEntity(workflow);
    workflow.setOperations(List.of(operation1));

    DB.save(goods);

    var dbGoods = DB.find(GoodsEntity.class).findOne();
    // sanity check asserts
    assertThat(dbGoods.getId()).isEqualTo(1L);
    assertThat(dbGoods.getWorkflowEntity().getId()).isEqualTo(1L);
    assertThat(dbGoods.getWorkflowEntity().getOperations()).hasSize(1);
    assertThat(dbGoods.getWorkflowEntity().getOperations().get(0).getId()).isEqualTo(1L);

    List<String> createSql = LoggedSql.stop();
    LoggedSql.start();

    // statelessly add another operation to the workflow and save goods
    var goodsStateless = new GoodsEntity();
    goodsStateless.setId(1L);
    var workflowStateless = new WorkflowEntity();
    workflowStateless.setId(1L);
    var operation1Stateless = new WorkflowOperationEntity();
    operation1Stateless.setId(1L);
    var operation2 = new WorkflowOperationEntity();
    goodsStateless.setWorkflowEntity(workflowStateless);
    workflowStateless.setOperations(List.of(operation1Stateless, operation2));


    /*
      - this update generates following statements
     1   txn[] delete from workflow_operation_entity where workflow_id=?
     2   txn[]  -- bind(1)
     3   txn[] update workflow_entity set when_modified=? where id=?
     4   txn[]  -- bind(2022-06-29 15:43:55.573,1)
     5   txn[] update workflow_operation_entity set deleted=true where workflow_id = ? and not ( id in (?) )
     6   txn[]  -- bind(1, Array[1]={1})
     7   txn[] insert into workflow_operation_entity (name, version, when_created, when_modified, deleted, workflow_id) values (?,?,?,?,?,?)
     8   txn[]  -- bind(null,1,2022-06-29 15:43:55.584,2022-06-29 15:43:55.584,false,1)
     9   txn[] update goods_entity set when_modified=?, workflow_entity_id=? where id=?; -- bind(2022-06-29 15:43:55.573,1,1)

      - number 1 is wrong
        - no delete should be issued
        - even if it was issued, it should have been soft delete
      - the DB.update will throw exception if there is one-to-many relation on workflow_operation_entity
              - it would still be referenced from other table
     */
    DB.update(goodsStateless);
    var updateSql = LoggedSql.stop();
    updateSql.forEach(System.out::println);

    var dbGoodsAfterUpdate = DB.find(GoodsEntity.class).findOne();
    assertThat(dbGoodsAfterUpdate.getWorkflowEntity().getOperations()).hasSize(2);
    assertThat(dbGoodsAfterUpdate.getWorkflowEntity().getOperations().get(0).getId()).isEqualTo(1L);
    assertThat(dbGoodsAfterUpdate.getWorkflowEntity().getOperations().get(1).getId()).isEqualTo(2L);
    updateSql.forEach(sql -> assertThat(sql).doesNotContain("delete from workflow_entity"));
  }

}

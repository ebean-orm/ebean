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

class TestOneToManyStatelessUpdateResultsInSoftDelete extends BaseTestCase {
  @Test
  void testStatelessUpdateShouldntDelete() {
    LoggedSql.start();
    var goods = new GoodsEntity();
    var workflow = new WorkflowEntity();
    var operation1 = new WorkflowOperationEntity();
    goods.setWorkflowEntity(workflow);
    workflow.setOperations(List.of(operation1));

    DB.save(goods);
    LoggedSql.collect();

    // statelessly add another operation to the workflow and save goods
    var goodsStateless = new GoodsEntity();
    goodsStateless.setId(goods.getId());
    var workflowStateless = new WorkflowEntity();
    workflowStateless.setId(workflow.getId());
    var operation1Stateless = new WorkflowOperationEntity();
    operation1Stateless.setId(operation1.getId());
    var operation2 = new WorkflowOperationEntity();
    goodsStateless.setWorkflowEntity(workflowStateless);
    workflowStateless.setOperations(List.of(operation1Stateless, operation2));

    // With the fix the SQL is now:
    /*
      txn[1002] update workflow_entity set when_modified=? where id=?
      txn[1002]  -- bind(2022-07-04 11:10:54.446,1)
      txn[1002] update workflow_operation_entity set deleted=true where workflow_id = ? and not ( id in (?) )
      txn[1002]  -- bind(1, Array[1]={1})
      txn[1002] insert into workflow_operation_entity (name, version, when_created, when_modified, deleted, workflow_id) values (?,?,?,?,?,?)
      txn[1002]  -- bind(null,1,2022-07-04 11:10:54.458,2022-07-04 11:10:54.458,false,1)
      txn[1002] update goods_entity set when_modified=?, workflow_entity_id=? where id=?; -- bind(2022-07-04 11:10:54.446,1,1)
     */

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
    //updateSql.forEach(System.out::println);
    var dbGoodsAfterUpdate = DB.find(GoodsEntity.class, goods.getId());
    assertThat(dbGoodsAfterUpdate.getWorkflowEntity().getOperations()).hasSize(2);
    assertThat(dbGoodsAfterUpdate.getWorkflowEntity().getOperations()).extracting("id").contains(operation1.getId(), operation2.getId());
    updateSql.forEach(sql -> assertThat(sql).doesNotContain("delete from workflow_entity"));
  }

  // same as previous but DB.update throws exception
  @Test
  void testStatelessUpdateShouldntDeleteThrows() {
    LoggedSql.start();
    var goods = new GoodsEntity();
    goods.setName("ver1");
    var workflow = new WorkflowEntity();
    workflow.setRevision("ver1");
    var operation1 = new WorkflowOperationEntity();
    operation1.setName("ver1");
    goods.setWorkflowEntity(workflow);
    workflow.setOperations(List.of(operation1));

    DB.save(goods);

    List<String> createSql = LoggedSql.stop();
    LoggedSql.start();

    // statelessly add another operation to the workflow and save goods
    var goodsStateless = new GoodsEntity();
    goodsStateless.setId(goods.getId());
    goodsStateless.setName("ver2");
    var workflowStateless = new WorkflowEntity();
    workflowStateless.setRevision("ver2");
    workflowStateless.setId(workflow.getId());
    var operation1Stateless = new WorkflowOperationEntity();
    operation1Stateless.setName("ver2");
    operation1Stateless.setId(operation1.getId());
    var operation2 = new WorkflowOperationEntity();
    operation2.setName("ver2");
    goodsStateless.setWorkflowEntity(workflowStateless);
    workflowStateless.setOperations(List.of(operation1Stateless, operation2));


    // throws
    DB.update(goodsStateless);
    var updateSql = LoggedSql.stop();
    updateSql.forEach(System.out::println);
    var dbGoodsAfterUpdate = DB.find(GoodsEntity.class, goods.getId());
    assertThat(dbGoodsAfterUpdate.getWorkflowEntity().getOperations()).hasSize(2);
    assertThat(dbGoodsAfterUpdate.getWorkflowEntity().getOperations().get(0).getId()).isEqualTo(operation1.getId());
    assertThat(dbGoodsAfterUpdate.getWorkflowEntity().getOperations().get(1).getId()).isEqualTo(operation2.getId());
    updateSql.forEach(sql -> assertThat(sql).doesNotContain("delete from workflow_entity"));
  }

  @Test
  void duplicateKeyWorkflowEntityInsertInsteadOfUpdate() {
    var goods = new GoodsEntity();
    goods.setName("ver1");
    var workflow = new WorkflowEntity();
    workflow.setRevision("ver1");
    var operation1 = new WorkflowOperationEntity();
    operation1.setName("ver1");
    goods.setWorkflowEntity(workflow);
    workflow.setOperations(List.of(operation1));

    DB.save(goods);

    // delete operation
    var goodsAfterInsert = DB.find(GoodsEntity.class, goods.getId());
    assertThat(goodsAfterInsert.getWorkflowEntity().getOperations()).hasSize(1);
    goodsAfterInsert.getWorkflowEntity().setOperations(List.of());

    DB.save(goodsAfterInsert);
    assertThat(goodsAfterInsert.getWorkflowEntity().getOperations()).isEmpty();
    assertThat(DB.find(GoodsEntity.class, goods.getId()).getWorkflowEntity().getOperations()).isEmpty();

    // statelessly add new WorkflowOperationEntity
    var goodsStateless = new GoodsEntity();
    goodsStateless.setId(goods.getId());

    var workflowStateless = new WorkflowEntity();
    workflowStateless.setId(workflow.getId());
    goodsStateless.setWorkflowEntity(workflowStateless);

    var operation2 = new WorkflowOperationEntity();
    workflowStateless.setOperations(List.of(operation2));

    // Using save() throws io.ebean.DuplicateKeyException: Error when batch flush on sql: insert into workflow_entity ...
    // Must be an update() and not save() for this to be a "stateless update"
    DB.update(goodsStateless);

    var ops = workflow.getOperations();
    // shouldn't contain deleted operations
    assertThat(ops).hasSize(1);
    assertThat(goodsStateless.getWorkflowEntity().getOperations().get(0).getId()).isNotEqualTo(operation1.getId());
  }
}

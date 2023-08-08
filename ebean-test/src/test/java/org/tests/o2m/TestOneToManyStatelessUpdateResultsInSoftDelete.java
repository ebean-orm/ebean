package org.tests.o2m;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.o2m.dm.*;

import java.io.StringWriter;
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

    LoggedSql.start();
    DB.save(goodsAfterInsert);
    var sql = LoggedSql.collect();
    if (isH2() || isPostgresCompatible()) { // using deleted=true vs deleted=1
      assertThat(sql).hasSize(2);
      assertThat(sql.get(0)).contains("update workflow_operation_entity set deleted=true where workflow_id = ?");
    }

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
    LoggedSql.collect();
    DB.update(goodsStateless);
    sql = LoggedSql.collect();
    assertThat(sql).isNotEmpty();
    if (isH2() || isPostgresCompatible()) { // using deleted=true vs deleted=1
      assertThat(sql).hasSize(7);
      assertThat(sql.get(0)).contains("update workflow_entity set when_modified=? where id=?");
      assertThat(sql.get(1)).contains(" -- bind(");
      assertThat(sql.get(2)).contains("update workflow_operation_entity set deleted=true where workflow_id = ?");
      assertThat(sql.get(3)).contains(" -- bind(");
      assertThat(sql.get(4)).contains("insert into workflow_operation_entity (name, version, when_created, when_modified");
      assertThat(sql.get(5)).contains(" -- bind(");
      assertThat(sql.get(6)).contains("update goods_entity set when_modified=?, workflow_entity_id=? where id=?");
    }

    var ops = workflow.getOperations();
    // shouldn't contain deleted operations
    assertThat(ops).hasSize(1);
    assertThat(goodsStateless.getWorkflowEntity().getOperations().get(0).getId()).isNotEqualTo(operation1.getId());

    LoggedSql.stop();
  }

  @Test
  void softDeleteIncludedInQuery() throws Exception {
    var defaultPerson = new PersonEntity();
    defaultPerson.setName("test");
    DB.save(defaultPerson);

    // create GoodsEntity with 1 WorkflowOperationEntity
    var goods = new GoodsEntity();
    goods.setCreatedBy(defaultPerson);
    goods.setName("ver1");
    var workflow = new WorkflowEntity();
    workflow.setRevision("ver1");
    var operation1 = new WorkflowOperationEntity();
    operation1.setName("ver1");
    goods.setWorkflowEntity(workflow);
    workflow.setOperations(List.of(operation1));

    DB.save(goods);

    // statelessly delete WorkflowOperationEntity
    var goodsStateless = new GoodsEntity();
    goodsStateless.setId(goods.getId());
    var workflowStateless = new WorkflowEntity();
    workflowStateless.setId(workflow.getId());
    goodsStateless.setWorkflowEntity(workflowStateless);
    workflowStateless.setOperations(List.of());

    LoggedSql.start();
    DB.update(goodsStateless);
    // uncommenting this lines makes the test pass
    //assertThat(goodsStateless.getWorkflowEntity().getOperations().size()).isEqualTo(0);

    var sql = LoggedSql.collect();
    if (isH2() || isPostgresCompatible()) { // using deleted=true vs deleted=1
      assertThat(sql).hasSize(5);
      assertThat(sql.get(0)).contains("update workflow_entity set when_modified=? where id=?");
      assertThat(sql.get(1)).contains(" -- bind(");
      assertThat(sql.get(2)).contains("update workflow_operation_entity set deleted=true where workflow_id = ?");
      assertThat(sql.get(3)).contains(" -- bind(");
      assertThat(sql.get(4)).contains("update goods_entity set when_modified=?, workflow_entity_id=? where id=?");
    }

    try (var writer = new StringWriter()) {
      var mapper = new ObjectMapper();
      mapper.writeValue(writer, goodsStateless);
      sql = LoggedSql.stop();
      // queries fired to load the object graph for writing as json
      assertThat(sql).hasSize(7);
      sql.forEach(s -> assertThat(s).startsWith("select "));
      /*
      select t0.id, t0.name, t0.workflow_entity_id, t0.version, t0.when_created, t0.when_modified from goods_entity t0 where t0.id = ?; --bind(4, ) --micros(161)
      select t0.id, t0.name, t0.version, t0.when_created, t0.when_modified, t0.created_by, t0.updated_by, t0.workflow_entity_id from goods_entity t0 where t0.id = ?; --bind(4, ) --micros(525)
      select t0.id, t0.name, t0.version, t0.when_created, t0.when_modified from person_entity t0 where t0.id = ?; --bind(1, ) --micros(325)
      ! is this even issue? - select does not check if workflow_entity is deleted
      select t0.id, t0.revision, t0.version, t0.when_created, t0.when_modified from workflow_entity t0 where t0.id = ?; --bind(1, ) --micros(439)
      select t0.id, t0.revision, t0.version, t0.when_created, t0.when_modified, t0.created_by, t0.updated_by from workflow_entity t0 where t0.id = ?; --bind(1, ) --micros(332)

      select t0.id, t0.name, t0.version, t0.when_created, t0.when_modified from person_entity t0 where t0.id = ?; --bind(1, ) --micros(197)
      select t0.workflow_id, t0.id, t0.position, t0.name, t0.workflow_id, t0.version, t0.when_created, t0.when_modified, t0.deleted from workflow_operation_entity t0 where (t0.workflow_id) in (?) order by t0.workflow_id, t0.position; --bind(Array[1]={1}) --micros(2776)
      select t0.id, t0.position, t0.name, t0.version, t0.when_created, t0.when_modified, t0.deleted, t0.workflow_id, t0.created_by, t0.updated_by from workflow_operation_entity t0 where t0.id = ?; --bind(1, ) --micros(415)

     !! ignores soft delete
       also to note - when the DM extends BaseDomain instead of HistoryColumns, this bug does not happen
       (presumably since @WhoCreated Person createdBy is lazy loaded, when it is eagerly loaded, this bug does not occur)
      select t0.workflow_id, t0.id, t0.position, t0.name, t0.workflow_id, t0.version, t0.when_created, t0.when_modified, t0.deleted from workflow_operation_entity t0 where (t0.workflow_id) in (?) order by t0.workflow_id, t0.position; --bind(Array[1]={4}) --micros(585)

      select t0.id, t0.position, t0.name, t0.version, t0.when_created, t0.when_modified, t0.deleted, t0.created_by, t0.updated_by, t0.workflow_id from workflow_operation_entity t0 where t0.id = ?; --bind(7, ) --micros(532)
      */

      writer.flush();
      var serialized = writer.toString();
      // System.out.println(serialized);
      assertThat(serialized).isNotEmpty();
      var readGoods = mapper.readValue(writer.toString(), GoodsEntity.class);
      assertThat(readGoods.getWorkflowEntity().getOperations()).hasSize(0);
    }
  }

  @Test
  void setArrayListDoInsertInsteadUpdate() {
    var attachment1 = new Attachment();
    attachment1.setName("File1");
    var attachment2 = new Attachment();
    attachment2.setName("File2");

    var goods = new GoodsEntity();
    goods.setName("goods1");
    goods.setAttachments(List.of(attachment1, attachment2));

    DB.save(goods);

    String goodAsJson = DB.json().toJson(goods);
    var marshaledGoods = DB.json().toBean(GoodsEntity.class, goodAsJson);
    var attachment3a = new Attachment();
    attachment3a.setName("File3");
    marshaledGoods.getAttachments().add(attachment3a);

    var persistedGoods = DB.find(GoodsEntity.class, goods.getId());

    // this was not deleting orphans and so the inserts throw exception due primary key conflict
    persistedGoods.setAttachments(marshaledGoods.getAttachments());
    LoggedSql.start();
    try {
      logger.info("Saving goods with set new attachments list");
      DB.save(persistedGoods);
    } catch (Exception e) {
      logger.error("Insert instead update", e);
    }
    var sql = LoggedSql.collect();
    assertThat(sql).isNotEmpty();
    assertThat(sql.get(0)).contains("delete from attachment where goods_entity_id = ?");
    assertThat(sql.get(1)).contains(" -- bind(");
    assertThat(sql.get(2)).contains("insert into attachment (id, goods_entity_id, name, ");
    assertThat(sql.get(3)).contains(" -- bind(");
    assertThat(sql.get(4)).contains(" -- bind(");
    if (isH2() || isPostgresCompatible() || isMySql()) { // i.e. using identity, not using sequence
      assertThat(sql.get(5)).contains("insert into attachment (goods_entity_id, name,");
      assertThat(sql.get(6)).contains(" -- bind(");
    }

    persistedGoods = DB.find(GoodsEntity.class, goods.getId());

    // this works good using .clear() and .addAll()
    // because here we are mutating the collection, it detects the
    // orphan removals occurring via the .clear()
    marshaledGoods = DB.json().toBean(GoodsEntity.class, goodAsJson);
    var attachment3b = new Attachment();
    attachment3b.setName("File3");
    marshaledGoods.getAttachments().add(attachment3b);

    persistedGoods.getAttachments().clear();
    persistedGoods.getAttachments().addAll(marshaledGoods.getAttachments());
    LoggedSql.collect();
    try {
      logger.info("Saving goods with clear/added attachments");
      DB.save(persistedGoods);
    } catch (Exception e) {
      logger.error("Insert instead update", e);
    }
    sql = LoggedSql.stop();
    assertThat(sql.get(0)).contains("delete from attachment where id=?");
    assertThat(sql.get(1)).contains(" -- bind(");
    assertThat(sql.get(2)).contains(" -- bind(");
    assertThat(sql.get(3)).contains(" -- bind(");
    assertThat(sql.get(4)).contains("insert into attachment (id, goods_entity_id, name,");
    assertThat(sql.get(5)).contains(" -- bind(");
    assertThat(sql.get(6)).contains(" -- bind(");
    if (isH2() || isPostgresCompatible()) { // using identity, not using sequence
      assertThat(sql).hasSize(9);
      assertThat(sql.get(7)).contains("insert into attachment (goods_entity_id, name, ");
      assertThat(sql.get(8)).contains(" -- bind(");
    }

    var persistedGoods2 = DB.find(GoodsEntity.class, goods.getId());
    assertThat(persistedGoods2.getAttachments()).hasSize(3);
  }
}

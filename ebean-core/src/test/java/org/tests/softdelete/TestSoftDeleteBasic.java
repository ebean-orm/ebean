package org.tests.softdelete;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import io.ebean.Transaction;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.softdelete.EBasicNoSDChild;
import org.tests.model.softdelete.EBasicSDChild;
import org.tests.model.softdelete.EBasicSoftDelete;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class TestSoftDeleteBasic extends BaseTestCase {

  @Test
  public void testCascadeSaveDelete_other() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("cascadeOne");
    bean.addChild("child1", 10);
    bean.addChild("child2", 20);
    bean.addChild("child3", 30);
    bean.addNoSoftDeleteChild("nsd1", 101);
    bean.addNoSoftDeleteChild("nsd2", 102);

    DB.save(bean);

    assertThat(bean.getVersion()).isEqualTo(1);
    assertThat(bean.getChildren().get(0).getVersion()).isEqualTo(1);

    LoggedSqlCollector.start();

    DB.delete(bean);

//    List<String> loggedSql = LoggedSqlCollector.stop();
//
//    assertEquals(new Long(2), bean.getVersion());
//    assertEquals(new Long(2), bean.getChildren().get(0).getVersion()); // Fails with 1
//
//    // The children without SoftDelete are left as is (so no third statement)
//    assertThat(loggedSql).hasSize(2);
//
//    // first statement is a single bulk update of the children with SoftDelete
//    assertThat(loggedSql.get(0)).contains("update ebasic_sdchild set deleted=");
//    assertThat(loggedSql.get(0)).contains("where owner_id = ?");
//
//    // second statement is the top level bean
//    assertThat(loggedSql.get(1)).contains(
//      "update ebasic_soft_delete set version=?, deleted=? where id=? and version=?");

  }

  @Test
  public void testFindIdsWhenIncludeSoftDeletedChlld() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("softDelChildren");
    bean.addChild("child1", 10);
    bean.addChild("child2", 20);
    bean.addChild("child3", 30);

    DB.save(bean);
    DB.delete(bean.getChildren().get(0));

    LoggedSqlCollector.start();

    List<Object> ids = DB.find(EBasicSDChild.class).where().eq("owner", bean).findIds();
    assertThat(ids).hasSize(2);

    List<EBasicSDChild> beans = DB.find(EBasicSDChild.class).where().eq("owner", bean).findList();
    assertThat(beans).hasSize(2);

    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("from ebasic_sdchild t0 where t0.owner_id = ? and t0.deleted = ");
    assertSql(sql.get(1)).contains("from ebasic_sdchild t0 where t0.owner_id = ? and t0.deleted = ");
  }

  @Test
  public void test() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("one");
    DB.save(bean);

    DB.delete(bean);

    SqlQuery sqlQuery = DB.sqlQuery("select * from ebasic_soft_delete where id=?");
    sqlQuery.setParameter(bean.getId());
    SqlRow sqlRow = sqlQuery.findOne();
    assertThat(sqlRow).isNotNull();

    EBasicSoftDelete findNormal = DB.find(EBasicSoftDelete.class)
      .setId(bean.getId())
      .findOne();

    assertThat(findNormal).isNull();

    EBasicSoftDelete findInclude = DB.find(EBasicSoftDelete.class)
      .setId(bean.getId())
      .setIncludeSoftDeletes()
      .findOne();

    assertThat(findInclude).isNotNull();
  }

  @Test
  public void testFindSoftDeleted() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("softDelFetch");
    DB.save(bean);

    DB.delete(bean);

    Query<EBasicSoftDelete> query = DB.find(EBasicSoftDelete.class)
      .setIncludeSoftDeletes()
      .where()
      .eq("deleted", true)
      .eq("name", "softDelFetch")
      .query();

    List<EBasicSoftDelete> list = query.findList();
    assertSql(query).contains("deleted = ?");
    assertThat(list).hasSize(1);

    // Cleanup created entity
    query.delete();
  }

  @Test
  public void testFindChild_joinParent() {
    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("softDelParent_withChild");
    bean.addChild("child1", 10);

    DB.save(bean);

    Query<EBasicSDChild> query = DB.find(EBasicSDChild.class)
      .where()
      .eq("owner.name", "softDelParent_withChild")
      .query();

    List<EBasicSDChild> list = query.findList();
    assertSql(query).contains("join ebasic_soft_delete t1 on t1.id = t0.owner_id and t1.deleted =");
    assertThat(list).hasSize(1);

    // Cleanup created entity
    DB.deletePermanent(bean);
  }

  @Test
  public void testFindSoftDeletedList() {
    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("softDelFetch");
    DB.save(bean);

    EBasicSDChild bean2 = new EBasicSDChild(bean, "softDelFetchus", 1L);
    DB.save(bean2);
    DB.delete(bean2);

    EBasicSDChild child = DB
      .find(EBasicSDChild.class)
      .setIncludeSoftDeletes()
      .where()
      .idEq(bean2.getId())
      .findOne();
    assertThat(child).isNotNull();
    assertThat(child.getOwner().getChildren().size()).isEqualTo(1);
  }


  @Test
  public void testNotFindSoftDeleted() {
    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("Shouldn't be found with child expression when child is deleted");
    DB.save(bean);
    final EBasicSDChild child = new EBasicSDChild(bean, "Delete me, don't find me", 1L);
    DB.save(child);
    DB.delete(child);

    Query<EBasicSoftDelete> query = DB.find(EBasicSoftDelete.class)
                                         .where()
                                         .eq("id", bean.getId())
                                         .eq("children.amount", 1L)
                                         .query();

    List<EBasicSoftDelete> list = query.findList();
    if (isMySql() || isMariaDB()) {
      assertSql(query).contains("t0.deleted = 0");
      assertSql(query).contains("u1.deleted = 0");
    } else {
      assertSql(query).contains("t0.deleted = false");
      // Make sure that query includes that the child mustn't've been deleted
      assertSql(query).contains("u1.deleted = false");
    }
    assertThat(list).hasSize(0);

    // Cleanup created entity
    DB.deleteAllPermanent(singletonList(child));
    DB.deleteAllPermanent(singletonList(bean));
  }

  @Test
  public void testNotFindSoftDeletedMultilevel() {
    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("Shouldn't be found with child expression when child is deleted");
    DB.save(bean);
    final EBasicSDChild child = new EBasicSDChild(bean, "Delete me, don't find me", 1L);
    DB.save(child);
    DB.delete(child);

    EBasicNoSDChild secondChild = new EBasicNoSDChild(bean, "Never deleted", 2L);
    DB.save(secondChild);


    Query<EBasicNoSDChild> query = DB.find(EBasicNoSDChild.class)
                                      .where()
                                      .eq("id", bean.getId())
                                      .eq("owner.children.amount", 1L)
                                      .query();

    List<EBasicNoSDChild> list = query.findList();
    // Make sure that query includes that the child mustn't've been deleted
    if (isMySql() || isMariaDB()) {
      assertSql(query).contains("u1.deleted = 0");
      assertSql(query).contains("u2.deleted = 0");
    } else {
      assertSql(query).contains("u1.deleted = false");
      assertSql(query).contains("u2.deleted = false");
    }
    assertThat(list).hasSize(0);

    // Cleanup created entity
    DB.deleteAllPermanent(Arrays.asList(child, secondChild));
    DB.deleteAllPermanent(singletonList(bean));
  }

  @Test
  public void testNotFindSoftDeletedMultilevel_with_setIncludeSoftDeletes() {
    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("Found with child expression");
    DB.save(bean);
    final EBasicSDChild child = new EBasicSDChild(bean, "SoftDelete me", 1L);
    DB.save(child);
    DB.delete(child);

    EBasicNoSDChild secondChild = new EBasicNoSDChild(bean, "Never deleted", 2L);
    DB.save(secondChild);


    Query<EBasicNoSDChild> query = DB.find(EBasicNoSDChild.class)
      .setIncludeSoftDeletes()
      .where()
      .eq("id", bean.getId())
      .eq("owner.children.amount", 1L)
      .query();

    List<EBasicNoSDChild> list = query.findList();
    // Make sure that query not not includes the soft delete join predicates
    assertSql(query).doesNotContain("u1.deleted = false");
    assertSql(query).doesNotContain("u2.deleted = false");
    assertThat(list).hasSize(1);

    // Cleanup created entity
    DB.deleteAllPermanent(Arrays.asList(child, secondChild));
    DB.deleteAllPermanent(singletonList(bean));
  }

  @Test
  public void testDeleteById_and_findCount() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("two");
    DB.save(bean);

    int rowCountBefore = DB.find(EBasicSoftDelete.class).findCount();

    DB.delete(EBasicSoftDelete.class, bean.getId());


    // -- test .findCount()

    LoggedSqlCollector.start();
    int rowCountAfter = DB.find(EBasicSoftDelete.class).findCount();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("where t0.deleted =");

    assertThat(rowCountAfter).isEqualTo(rowCountBefore - 1);

    // -- test includeSoftDeletes().findCount()

    LoggedSqlCollector.start();
    int rowCountFull = DB.find(EBasicSoftDelete.class).setIncludeSoftDeletes().findCount();
    assertThat(rowCountFull).isGreaterThan(rowCountAfter);

    loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).doesNotContain("where coalesce(t0.deleted,false)=false");
  }

  @Test
  public void testDeletePartial() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("partial");
    DB.save(bean);

    // partially loaded bean without deleted state loaded
    EBasicSoftDelete partial = DB.find(EBasicSoftDelete.class)
      .select("id")
      .setId(bean.getId())
      .findOne();

    LoggedSqlCollector.start();
    DB.delete(partial);

    // check lazy loading isn't invoked (deleted set to true without invoking lazy loading)
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(2);
    assertThat(loggedSql.get(0)).contains("update ebasic_sdchild set deleted=");
    assertThat(loggedSql.get(1)).contains("update ebasic_soft_delete set deleted=? where id=?");
  }

  @Test
  public void testCascadeSaveDelete() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("cascadeOne");
    bean.addChild("child1", 10);
    bean.addChild("child2", 20);
    bean.addChild("child3", 30);
    bean.addNoSoftDeleteChild("nsd1", 101);
    bean.addNoSoftDeleteChild("nsd2", 102);

    DB.save(bean);

    LoggedSqlCollector.start();

    DB.delete(bean);

    List<String> loggedSql = LoggedSqlCollector.stop();

    // The children without SoftDelete are left as is (so no third statement)
    assertThat(loggedSql).hasSize(2);

    // first statement is a single bulk update of the children with SoftDelete
    assertThat(loggedSql.get(0)).contains("update ebasic_sdchild set deleted=");
    assertThat(loggedSql.get(0)).contains("where owner_id = ?");

    // second statement is the top level bean
    assertThat(loggedSql.get(1)).contains("update ebasic_soft_delete set version=?, deleted=? where id=? and version=?");

  }


  @Test
  public void testFetch() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("cascadeOne");
    bean.addChild("child1", 10);
    bean.addChild("child2", 20);
    bean.addChild("child3", 30);
    bean.addNoSoftDeleteChild("nsd1", 101);
    bean.addNoSoftDeleteChild("nsd2", 102);

    DB.save(bean);

    DB.delete(bean.getChildren().get(1));

    LoggedSqlCollector.start();

    Query<EBasicSoftDelete> query1 =
      DB.find(EBasicSoftDelete.class)
        .fetch("children")
        .where().eq("id", bean.getId())
        .query();

    List<EBasicSoftDelete> fetch1 = query1.findList();
    String generatedSql = query1.getGeneratedSql();

    // first statement is a single bulk update of the children with SoftDelete
    assertThat(generatedSql).contains("t0.deleted = ");
    assertThat(generatedSql).contains("t1.deleted = ");
    assertThat(fetch1.get(0).getChildren()).hasSize(2);

    assertThat(fetch1.get(0).getNosdChildren()).hasSize(2);


    // fetch again using lazy loading
    EBasicSoftDelete fetchWithLazy = DB.find(EBasicSoftDelete.class, bean.getId());
    assertThat(fetchWithLazy.getChildren()).hasSize(2);

    // fetch includeSoftDeletes using lazy loading
    EBasicSoftDelete fetchAllWithLazy =
      DB.find(EBasicSoftDelete.class)
        .setId(bean.getId())
        .where()
        .setIncludeSoftDeletes()
        .findOne();

    assertThat(fetchAllWithLazy.getChildren()).hasSize(3);
  }

  @Test
  public void testWhenAllChildrenSoftDeleted() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("softDelChildren");
    bean.addChild("child1", 10);
    bean.addChild("child2", 20);

    DB.save(bean);
    DB.deleteAll(bean.getChildren());

    Query<EBasicSoftDelete> query = DB.find(EBasicSoftDelete.class)
      .setId(bean.getId())
      .fetch("children");

    EBasicSoftDelete found = query.findOne();

    String generatedSql = sqlOf(query);

    if (isPlatformBooleanNative()) {
      assertThat(generatedSql).contains("left join ebasic_sdchild t1 on t1.owner_id = t0.id and t1.deleted = false");
      assertThat(generatedSql).contains("t0.deleted = false");
    } else {
      assertThat(generatedSql).contains("left join ebasic_sdchild t1 on t1.owner_id = t0.id and t1.deleted = 0");
      assertThat(generatedSql).contains("t0.deleted = 0");
    }
    assertThat(found).isNotNull();
    assertThat(found.getChildren()).hasSize(0);
  }

  @Test
  public void testFetchWithIncludeSoftDeletes() {

    try (Transaction txn = DB.beginTransaction()) {
      EBasicSoftDelete bean = new EBasicSoftDelete();
      bean.setName("fetchWithInclude");
      bean.addChild("child1", 91);
      bean.addChild("child2", 92);

      DB.save(bean);

      EBasicSDChild child0 = bean.getChildren().get(0);

      EBasicSDChild upd = new EBasicSDChild();
      upd.setId(child0.getId());
      upd.setDeleted(true);
      DB.update(upd);

      Query<EBasicSoftDelete> query = DB.find(EBasicSoftDelete.class)
        .fetch("children")
        .setIncludeSoftDeletes()
        .where().eq("name", "fetchWithInclude")
        .query();

      List<EBasicSoftDelete> top = query.findList();
      assertThat(top).hasSize(1);
      assertThat(top.get(0).getChildren()).hasSize(2);
    }
  }
}

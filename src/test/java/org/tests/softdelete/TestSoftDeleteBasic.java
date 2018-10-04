package org.tests.softdelete;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import io.ebean.Transaction;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.softdelete.EBasicSDChild;
import org.tests.model.softdelete.EBasicSoftDelete;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSoftDeleteBasic extends BaseTestCase {

  @Test
  public void testFindIdsWhenIncludeSoftDeletedChlld() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("softDelChildren");
    bean.addChild("child1", 10);
    bean.addChild("child2", 20);
    bean.addChild("child3", 30);

    Ebean.save(bean);
    Ebean.delete(bean.getChildren().get(0));

    LoggedSqlCollector.start();

    List<Object> ids = Ebean.find(EBasicSDChild.class).where().eq("owner", bean).findIds();
    assertThat(ids).hasSize(2);

    List<EBasicSDChild> beans = Ebean.find(EBasicSDChild.class).where().eq("owner", bean).findList();
    assertThat(beans).hasSize(2);

    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("from ebasic_sdchild t0 where t0.owner_id = ?  and t0.deleted = false");
    assertThat(sql.get(1)).contains("from ebasic_sdchild t0 where t0.owner_id = ?  and t0.deleted = false");
  }

  @Test
  public void test() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("one");
    Ebean.save(bean);

    Ebean.delete(bean);

    SqlQuery sqlQuery = Ebean.createSqlQuery("select * from ebasic_soft_delete where id=?");
    sqlQuery.setParameter(1, bean.getId());
    SqlRow sqlRow = sqlQuery.findOne();
    assertThat(sqlRow).isNotNull();

    EBasicSoftDelete findNormal = Ebean.find(EBasicSoftDelete.class)
      .setId(bean.getId())
      .findOne();

    assertThat(findNormal).isNull();

    EBasicSoftDelete findInclude = Ebean.find(EBasicSoftDelete.class)
      .setId(bean.getId())
      .setIncludeSoftDeletes()
      .findOne();

    assertThat(findInclude).isNotNull();
  }

  @Test
  public void testFindSoftDeleted() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("softDelFetch");
    Ebean.save(bean);

    Ebean.delete(bean);

    Query<EBasicSoftDelete> query = Ebean.find(EBasicSoftDelete.class)
      .setIncludeSoftDeletes()
      .where()
      .eq("deleted", true)
      .eq("name", "softDelFetch")
      .query();

    List<EBasicSoftDelete> list = query.findList();
    assertThat(query.getGeneratedSql()).contains("deleted = ?");
    assertThat(list).hasSize(1);

    // Cleanup created entity
    query.delete();
  }

  @Test
  public void testDeleteById_and_findCount() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("two");
    Ebean.save(bean);

    int rowCountBefore = Ebean.find(EBasicSoftDelete.class).findCount();

    Ebean.delete(EBasicSoftDelete.class, bean.getId());


    // -- test .findCount()

    LoggedSqlCollector.start();
    int rowCountAfter = Ebean.find(EBasicSoftDelete.class).findCount();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("where t0.deleted =");

    assertThat(rowCountAfter).isEqualTo(rowCountBefore - 1);

    // -- test includeSoftDeletes().findCount()

    LoggedSqlCollector.start();
    int rowCountFull = Ebean.find(EBasicSoftDelete.class).setIncludeSoftDeletes().findCount();
    assertThat(rowCountFull).isGreaterThan(rowCountAfter);

    loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).doesNotContain("where coalesce(t0.deleted,false)=false");
  }

  @Test
  public void testDeletePartial() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("partial");
    Ebean.save(bean);

    // partially loaded bean without deleted state loaded
    EBasicSoftDelete partial = Ebean.find(EBasicSoftDelete.class)
      .select("id")
      .setId(bean.getId())
      .findOne();

    LoggedSqlCollector.start();
    Ebean.delete(partial);

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

    Ebean.save(bean);

    LoggedSqlCollector.start();

    Ebean.delete(bean);

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

    Ebean.save(bean);

    Ebean.delete(bean.getChildren().get(1));

    LoggedSqlCollector.start();

    Query<EBasicSoftDelete> query1 =
      Ebean.find(EBasicSoftDelete.class)
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
    EBasicSoftDelete fetchWithLazy = Ebean.find(EBasicSoftDelete.class, bean.getId());
    assertThat(fetchWithLazy.getChildren()).hasSize(2);

    // fetch includeSoftDeletes using lazy loading
    EBasicSoftDelete fetchAllWithLazy =
      Ebean.find(EBasicSoftDelete.class)
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

    Ebean.save(bean);
    Ebean.deleteAll(bean.getChildren());

    Query<EBasicSoftDelete> query = Ebean.find(EBasicSoftDelete.class)
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

    try (Transaction tyn = Ebean.beginTransaction()) {
      EBasicSoftDelete bean = new EBasicSoftDelete();
      bean.setName("fetchWithInclude");
      bean.addChild("child1", 91);
      bean.addChild("child2", 92);

      Ebean.save(bean);

      EBasicSDChild child0 = bean.getChildren().get(0);

      EBasicSDChild upd = new EBasicSDChild();
      upd.setId(child0.getId());
      upd.setDeleted(true);
      Ebean.update(upd);

      Query<EBasicSoftDelete> query = Ebean.find(EBasicSoftDelete.class)
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

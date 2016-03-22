package com.avaje.tests.softdelete;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.tests.model.softdelete.EBasicSoftDelete;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSoftDeleteBasic extends BaseTestCase {

  @Test
  public void test() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("one");
    Ebean.save(bean);

    Ebean.delete(bean);

    SqlQuery sqlQuery = Ebean.createSqlQuery("select * from ebasic_soft_delete where id=?");
    sqlQuery.setParameter(1, bean.getId());
    SqlRow sqlRow = sqlQuery.findUnique();
    assertThat(sqlRow).isNotNull();

    EBasicSoftDelete findNormal = Ebean.find(EBasicSoftDelete.class)
        .setId(bean.getId())
        .findUnique();

    assertThat(findNormal).isNull();

    EBasicSoftDelete findInclude = Ebean.find(EBasicSoftDelete.class)
        .setId(bean.getId())
        .includeSoftDeletes()
        .findUnique();

    assertThat(findInclude).isNotNull();
  }

  @Test
  public void testDeleteById_and_findRowCount() {

    EBasicSoftDelete bean = new EBasicSoftDelete();
    bean.setName("two");
    Ebean.save(bean);

    int rowCountBefore = Ebean.find(EBasicSoftDelete.class).findRowCount();

    Ebean.delete(EBasicSoftDelete.class, bean.getId());


    // -- test .findRowCount()

    LoggedSqlCollector.start();
    int rowCountAfter = Ebean.find(EBasicSoftDelete.class).findRowCount();

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    // usually where coalesce(t0.deleted,false)=false but false is 0 for Oracle etc
    assertThat(loggedSql.get(0)).contains("where coalesce(t0.deleted,");

    assertThat(rowCountAfter).isEqualTo(rowCountBefore - 1);

    // -- test includeSoftDeletes().findRowCount()

    LoggedSqlCollector.start();
    int rowCountFull = Ebean.find(EBasicSoftDelete.class).includeSoftDeletes().findRowCount();
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
        .findUnique();

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
    assertThat(generatedSql).contains("coalesce(t0.deleted,");
    assertThat(generatedSql).contains("coalesce(t1.deleted,");
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
        .includeSoftDeletes()
        .findUnique();

    assertThat(fetchAllWithLazy.getChildren()).hasSize(3);
  }
}

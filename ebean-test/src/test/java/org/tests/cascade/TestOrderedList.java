package org.tests.cascade;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOrderedList extends BaseTestCase {

  @Test
  public void test() {

    OmOrderedMaster master = new OmOrderedMaster("m1");

    List<OmOrderedDetail> details = master.getDetails();
    for (int i = 0; i < 5; i++) {
      details.add(new OmOrderedDetail("d"+i));
    }

    LoggedSql.start();
    DB.save(master);

    List<String> sql = LoggedSql.collect();
    assertThat(sql.size()).isGreaterThan(1);
    assertSql(sql.get(0)).contains("insert into om_ordered_master");
    boolean hasId = sql.get(1).contains(" (id, name");
    if (hasId) {
      assertSql(sql.get(1)).contains("insert into om_ordered_detail (id, name, version, sort_order, master_id) values (?,?,?,?,?)");
    } else {
      assertSql(sql.get(1)).contains("insert into om_ordered_detail (name, version, sort_order, master_id) values (?,?,?,?)");
    }

    // update without any changes
    DB.save(master);

    sql = LoggedSql.collect();
    assertThat(sql).isEmpty();


    // update just changing master
    master.setName("m1-mod");
    DB.save(master);
    sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("update om_ordered_master set name=?, version=?");

    fetchAndReorder(master.getId());
  }

  private void fetchAndReorder(Long id) {

    OmOrderedMaster fresh = DB.find(OmOrderedMaster.class).setId(id).fetch("details").findOne();
    List<OmOrderedDetail> details1 = fresh.getDetails();

    List<String> sql = LoggedSql.collect();
    assertSql(sql.get(0)).contains("order by t0.id, t1.sort_order");

    // fetched, not dirty
    DB.save(fresh);
    sql = LoggedSql.collect();
    assertThat(sql).isEmpty();


    // reorder
    OmOrderedDetail third = details1.remove(2);
    OmOrderedDetail first = details1.remove(0);
    details1.add(first);
    details1.add(third);

    fresh.setName("m1-reorder");

    DB.save(fresh);

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(8);
    assertSql(sql.get(0)).contains("update om_ordered_master set name=?, version=?");
    assertSql(sql.get(1)).contains("update om_ordered_detail set version=?, sort_order=? where id=? and version=?");

    details1.get(1).setName("was 1");
    fresh.setName("m1-mod3");
    DB.save(fresh);

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(4);
    assertSql(sql.get(0)).contains("update om_ordered_master set name=?, version=? where id=? and version=?; -- bind(m1-mod3");
    assertSql(sql.get(1)).contains("update om_ordered_detail set name=?, version=?, sort_order=? where id=? and version=?");
    assertThat(sql.get(2)).contains("bind(was 1,3,2,");

    DB.delete(fresh);

    sql = LoggedSql.stop();
    assertThat(sql).hasSize(4);
    assertSql(sql.get(0)).contains("delete from om_ordered_detail where master_id = ?");
    assertSqlBind(sql.get(1));
    assertSql(sql.get(3)).contains("delete from om_ordered_master where id=? and version=?");
  }

  @Test
  public void testAddSavedDetailToMaster() {
    final OmOrderedMaster master = new OmOrderedMaster("Master");
    final OmOrderedDetail detail = new OmOrderedDetail("Detail");

    DB.save(master);
    DB.save(detail);

    master.getDetails().add(detail);
    DB.save(master);

    final OmOrderedMaster masterDb = DB.find(OmOrderedMaster.class, master.getId());
    assertThat(masterDb.getDetails()).hasSize(1);
  }

  @Test
  public void testModifyList() {
    final OmOrderedMaster master = new OmOrderedMaster("Master");
    final OmOrderedDetail detail1 = new OmOrderedDetail("Detail1");
    final OmOrderedDetail detail2 = new OmOrderedDetail("Detail2");
    final OmOrderedDetail detail3 = new OmOrderedDetail("Detail3");
    DB.save(detail1);
    DB.save(detail2);
    DB.save(detail3);
    master.getDetails().add(detail1);
    master.getDetails().add(detail2);
    master.getDetails().add(detail3);

    DB.save(master);

    OmOrderedMaster masterDb = DB.find(OmOrderedMaster.class, master.getId());
    assertThat(masterDb.getDetails()).containsExactly(detail1, detail2, detail3);

    Collections.reverse(masterDb.getDetails());

    DB.save(masterDb);

    masterDb = DB.find(OmOrderedMaster.class, master.getId());
    assertThat(masterDb.getDetails()).containsExactly(detail3, detail2, detail1);

    masterDb.getDetails().remove(1);

    DB.save(masterDb);

    masterDb = DB.find(OmOrderedMaster.class, master.getId());
    assertThat(masterDb.getDetails()).containsExactly(detail3, detail1);

  }

  @Test
  public void testModifyListWithCache() {
    final OmCacheOrderedMaster master = new OmCacheOrderedMaster("Master");
    final OmCacheOrderedDetail detail1 = new OmCacheOrderedDetail("Detail1");
    final OmCacheOrderedDetail detail2 = new OmCacheOrderedDetail("Detail2");
    final OmCacheOrderedDetail detail3 = new OmCacheOrderedDetail("Detail3");
    DB.save(detail1);
    DB.save(detail2);
    DB.save(detail3);
    master.getDetails().add(detail1);
    master.getDetails().add(detail2);
    master.getDetails().add(detail3);

    DB.save(master);

    OmCacheOrderedMaster masterDb = DB.find(OmCacheOrderedMaster.class, master.getId()); // load cache
    assertThat(masterDb.getDetails()).containsExactly(detail1, detail2, detail3);

    masterDb = DB.find(OmCacheOrderedMaster.class, master.getId());
    assertThat(masterDb.getDetails()).containsExactly(detail1, detail2, detail3); // hit cache

    Collections.reverse(masterDb.getDetails());

    DB.save(masterDb);

    masterDb = DB.find(OmCacheOrderedMaster.class, master.getId());
    assertThat(masterDb.getDetails()).containsExactly(detail3, detail2, detail1); // load cache

    masterDb = DB.find(OmCacheOrderedMaster.class, master.getId());
    assertThat(masterDb.getDetails()).containsExactly(detail3, detail2, detail1); // hit cache

    masterDb.getDetails().remove(1);

    DB.save(masterDb);

    masterDb = DB.find(OmCacheOrderedMaster.class, master.getId());
    assertThat(masterDb.getDetails()).containsExactlyInAnyOrder(detail3, detail1);

  }

  @Test
  public void testModifyListWithCache2() {
    final OmCacheOrderedMaster master = new OmCacheOrderedMaster("Master");
    final OmCacheOrderedDetail detail1 = new OmCacheOrderedDetail("Detail1");
    final OmCacheOrderedDetail detail2 = new OmCacheOrderedDetail("Detail2");
    final OmCacheOrderedDetail detail3 = new OmCacheOrderedDetail("Detail3");
    DB.save(detail1);
    DB.save(detail2);
    DB.save(detail3);
    master.getDetails().add(detail1);
    master.getDetails().add(detail2);
    master.getDetails().add(detail3);

    DB.save(master);

    OmCacheOrderedMaster masterDb = DB.find(OmCacheOrderedMaster.class, master.getId()); // load cache
    assertThat(masterDb.getDetails()).containsExactly(detail1, detail2, detail3);

    masterDb = DB.find(OmCacheOrderedMaster.class, master.getId());
    assertThat(masterDb.getDetails()).containsExactly(detail1, detail2, detail3); // hit cache

    // 1 und 2 tauschen
    masterDb.getDetails().add(0, masterDb.getDetails().remove(1));
    DB.save(masterDb);

    masterDb.getDetails().remove(2);
    DB.save(masterDb);

    LoggedSql.start();
    OmCacheOrderedMaster masterDbNew = DB.find(OmCacheOrderedMaster.class, master.getId());
    masterDbNew.getDetails().size();
    assertThat(masterDbNew.getDetails()).containsExactly(detail2, detail1);
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1).first().asString()
      .startsWith("select t0.id, t1.id, t1.name, t1.version, t1.sort_order, t1.master_id from om_cache_ordered_master t0 left join om_cache_ordered_detail t1 on t1.master_id = t0.id where t0.id = ? order by t1.sort_order;");

    DB.cacheManager().clearAll();
    masterDbNew = DB.find(OmCacheOrderedMaster.class, master.getId());
    LoggedSql.start();
    masterDbNew.getDetails().size();
    sql = LoggedSql.stop();
    assertThat(sql).hasSize(1).first().asString()
      .startsWith("select t0.master_id, t0.id, t0.name, t0.version, t0.sort_order, t0.master_id from om_cache_ordered_detail t0 where (t0.master_id) in (?) order by t0.master_id, t0.sort_order;");

    assertThat(masterDbNew.getDetails()).containsExactly(detail2, detail1);
  }
}

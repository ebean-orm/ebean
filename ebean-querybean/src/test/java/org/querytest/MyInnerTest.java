package org.querytest;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.example.domain.MyInner;
import org.example.domain.query.QMyInner;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyInnerTest {

  @Test
  void insert_and_find() {
    MyInner myInner = new MyInner()
      .id(42).one("one").description("foo");

    DB.save(myInner);

    MyInner found = DB.find(MyInner.class, new MyInner.ID(42, "one"));
    assertThat(found).isNotNull();

    MyInner found2 = new QMyInner()
      .id.eq(42)
      .one.eq("one")
      .findOne();

    assertThat(found2).isNotNull();

    LoggedSql.start();

    MyInner found3 = new QMyInner()
      .one.eqIfPresent("one")
      .findOne();

    assertThat(found3).isNotNull();

    MyInner found4 = new QMyInner()
      .one.eqIfPresent(null)
      .description.eqIfPresent("foo")
      .findOne();

    assertThat(found4).isNotNull();

    MyInner found5 =  new QMyInner()
      .id.lt(99)
      .one.gtIfPresent(null)
      .one.geIfPresent(null)
      .one.ltIfPresent(null)
      .one.leIfPresent(null)
      .description.eqIfPresent("foo")
      .findOne();

    assertThat(found5).isNotNull();

    MyInner found6 =  new QMyInner()
      .id.lt(99)
      .one.gtIfPresent("o")
      .one.geIfPresent("o")
      .one.ltIfPresent("oz")
      .one.leIfPresent("oz")
      .id.gt(1)
      .findOne();

    assertThat(found6).isNotNull();

    MyInner found7 = new QMyInner()
      .one.icontainsIfPresent("n")
      .findOne();

    assertThat(found7).isNotNull();

    MyInner found8 = new QMyInner()
      .one.icontainsIfPresent(null)
      .description.eq("foo")
      .findOne();

    assertThat(found8).isNotNull();

    MyInner found9 = new QMyInner()
      .one.likeIfPresent("on%")
      .findOne();

    assertThat(found9).isNotNull();

    MyInner found10 = new QMyInner()
      .one.likeIfPresent(null)
      .description.eq("foo")
      .findOne();

    assertThat(found10).isNotNull();

    MyInner found11 = new QMyInner()
      .one.ilikeIfPresent("ON%")
      .findOne();

    assertThat(found11).isNotNull();

    MyInner found12 = new QMyInner()
      .one.ilikeIfPresent(null)
      .description.eq("foo")
      .findOne();

    assertThat(found12).isNotNull();

    MyInner found13 = new QMyInner()
      .one.istartsWithIfPresent("ON")
      .findOne();

    assertThat(found13).isNotNull();

    MyInner found14 = new QMyInner()
      .one.istartsWithIfPresent(null)
      .description.eq("foo")
      .findOne();

    assertThat(found14).isNotNull();

    MyInner found15 = new QMyInner()
      .one.startsWithIfPresent("on")
      .findOne();

    assertThat(found15).isNotNull();

    MyInner found16 = new QMyInner()
      .one.startsWithIfPresent(null)
      .description.eq("foo")
      .findOne();

    assertThat(found16).isNotNull();

    MyInner found17 = new QMyInner()
      .one.containsIfPresent("ne")
      .findOne();

    assertThat(found17).isNotNull();

    MyInner found18 = new QMyInner()
      .one.containsIfPresent(null)
      .description.eq("foo")
      .findOne();

    assertThat(found18).isNotNull();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(16);
    assertThat(sql.get(0)).contains("where t0.one = ?");
    assertThat(sql.get(1)).contains("where t0.description = ?");
    assertThat(sql.get(2)).contains("where t0.id < ? and t0.description = ?");
    assertThat(sql.get(3)).contains("where t0.id < ? and t0.one > ? and t0.one >= ? and t0.one < ? and t0.one <= ? and t0.id > ?");
    assertThat(sql.get(4)).contains("where lower(t0.one) like ?");
    assertThat(sql.get(5)).contains("where t0.description = ?");
    assertThat(sql.get(6)).contains("where t0.one like ?");
    assertThat(sql.get(7)).contains("where t0.description = ?");
    assertThat(sql.get(8)).contains("where lower(t0.one) like ?");
    assertThat(sql.get(9)).contains("where t0.description = ?");
    assertThat(sql.get(10)).contains("where lower(t0.one) like ?");
    assertThat(sql.get(11)).contains("where t0.description = ?");
    assertThat(sql.get(12)).contains("where t0.one like ?");
    assertThat(sql.get(13)).contains("where t0.description = ?");
    assertThat(sql.get(14)).contains("where t0.one like ?");
    assertThat(sql.get(15)).contains("where t0.description = ?");
  }
}

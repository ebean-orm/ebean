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

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(4);
    assertThat(sql.get(0)).contains("select t0.id, t0.one, t0.id, t0.one, t0.description from my_inner t0 where t0.one = ?;");
    assertThat(sql.get(1)).contains("select t0.id, t0.one, t0.id, t0.one, t0.description from my_inner t0 where t0.description = ?;");
    assertThat(sql.get(2)).contains("select t0.id, t0.one, t0.id, t0.one, t0.description from my_inner t0 where t0.id < ? and t0.description = ?;");
    assertThat(sql.get(3)).contains("select t0.id, t0.one, t0.id, t0.one, t0.description from my_inner t0 where t0.id < ? and t0.one > ? and t0.one >= ? and t0.one < ? and t0.one <= ? and t0.id > ?;");
  }
}

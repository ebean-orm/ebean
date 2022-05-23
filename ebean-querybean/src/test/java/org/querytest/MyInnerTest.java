package org.querytest;

import io.ebean.DB;
import org.example.domain.MyInner;
import org.example.domain.query.QMyInner;
import org.junit.jupiter.api.Test;

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
  }
}

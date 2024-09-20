package org.tests.query;

import io.ebean.DB;
import io.ebean.OrderBy;
import io.ebean.Paging;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.ForPlatform;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestQueryPaging extends BaseTestCase {

  @ForPlatform({Platform.H2, Platform.POSTGRES})
  @Test
  void example() {
    var orderBy = OrderBy.of("lastName desc nulls first, firstName asc");
    var paging = Paging.of(0, 100, orderBy);

    DB.find(Contact.class)
      .setPaging(paging)
      .where().startsWith("lastName", "foo")
      .findList();
    // or instead of findList() use another find method like ...
    // findPagedList(), findEach(), findStream(), findMap(), findSet(), findSingleAttributeList(),

    var nextPage = paging.withPage(1);

    DB.find(Contact.class)
      .setPaging(nextPage)
      .findList();
  }

  @Test
  void whenNoOrderBy_expect_orderByIdUsed() {
    ResetBasicData.reset();

    LoggedSql.start();

    DB.find(Contact.class).select("lastName").setPaging(Paging.of(0, 4)).findList();
    DB.find(Contact.class).select("lastName").setPaging(Paging.of(2, 4)).findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    if (isLimitOffset()) {
      assertThat(sql.get(0)).contains("order by t0.id limit 4");
      assertThat(sql.get(1)).contains("order by t0.id limit 4 offset 8");
    }
  }

  @Test
  void whenOrderBy_expect_noExtraIdInTheOrderBy() {
    ResetBasicData.reset();

    LoggedSql.start();

    DB.find(Contact.class).select("lastName").setPaging(Paging.of(1, 4, "lastName")).findList();
    DB.find(Contact.class).select("lastName").setPaging(Paging.of(1, 4, "lastName, id")).findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    if (isLimitOffset()) {
      assertThat(sql.get(0)).contains("order by t0.last_name limit 4 offset 4");
      assertThat(sql.get(1)).contains("order by t0.last_name, t0.id limit 4 offset 4");
    }
  }

  @Test
  void whenNone_expect_noLimitOffsetAtAll() {
    ResetBasicData.reset();

    LoggedSql.start();

    DB.find(Contact.class).select("lastName").setPaging(Paging.ofNone()).findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("select t0.id, t0.last_name from contact t0;");
  }

  @Test
  void withPage() {
    Paging paging = Paging.of(0, 100);
    assertThat(paging.pageIndex()).isEqualTo(0);
    assertThat(paging.pageSize()).isEqualTo(100);

    Paging pg1 = paging.withPage(1);
    assertThat(pg1.pageIndex()).isEqualTo(1);
    assertThat(pg1.pageSize()).isEqualTo(100);

    Paging pg6 = paging.withPage(6);
    assertThat(pg6.pageIndex()).isEqualTo(6);
    assertThat(pg6.pageSize()).isEqualTo(100);
  }

  @Test
  void withOrderBy() {
    Paging pg1 = Paging.of(1, 100);
    assertThat(pg1.pageIndex()).isEqualTo(1);
    assertThat(pg1.pageSize()).isEqualTo(100);

    Paging pgWithOrder = pg1.withOrderBy("lastName desc nulls first, firstName asc");
    OrderBy<?> orderBy = pgWithOrder.orderBy();
    List<OrderBy.Property> properties = orderBy.getProperties();
    assertThat(properties).hasSize(2);
    assertThat(properties.get(0).getProperty()).isEqualTo("lastName");
    assertThat(properties.get(0).isAscending()).isFalse();
    assertThat(properties.get(1).getProperty()).isEqualTo("firstName");
    assertThat(properties.get(1).isAscending()).isTrue();
  }
}

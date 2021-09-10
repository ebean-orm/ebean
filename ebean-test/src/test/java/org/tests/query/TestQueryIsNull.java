package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.m2m.Role;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryIsNull extends BaseTestCase {


  @Test
  public void queryShouldContainIsNullOnColumn() {
    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class).where().isNull("customerName").query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("name is null");
  }

  @Test
  public void isNotNull_when_OneToMany_expect_existsSubquery() {
    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class).where().isNotNull("details").query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains(" where exists (select 1 from o_order_detail x where x.order_id = t0.id and x.id > 0)");
  }

  @Test
  public void isNotEmpty_when_OneToMany_expect_existsSubquery() {
    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class).where().isNotEmpty("details").query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains(" where exists (select 1 from o_order_detail x where x.order_id = t0.id and x.id > 0)");
  }

  @Test
  public void isNull_when_OneToMany_expect_notExistsSubquery() {
    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class).where().isNull("details").query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains(" where not exists (select 1 from o_order_detail x where x.order_id = t0.id and x.id > 0)");
  }

  @Test
  public void isEmpty_when_OneToMany_expect_notExistsSubquery() {
    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class).where().isEmpty("details").query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains(" where not exists (select 1 from o_order_detail x where x.order_id = t0.id and x.id > 0)");
  }

  @Test
  public void isEmpty_when_ManyToMany_expect_notExistsSubqueryAndNoJoin() {
    ResetBasicData.reset();

    Query<Role> query = DB.find(Role.class).where().isEmpty("permissions").query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("from mt_role t0 where not exists (select 1 from mt_role_permission x where x.mt_role_id = t0.id)");
  }

  @Test
  public void isNull_when_ManyToMany_expect_notExistsSubqueryAndNoJoin() {
    ResetBasicData.reset();

    Query<Role> query = DB.find(Role.class).where().isNull("permissions").query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("from mt_role t0 where not exists (select 1 from mt_role_permission x where x.mt_role_id = t0.id)");
  }

  @Test
  public void isNotEmpty_when_ManyToMany_expect_existsSubqueryAndNoJoin() {
    ResetBasicData.reset();

    Query<Role> query = DB.find(Role.class).where().isNotEmpty("permissions").query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("from mt_role t0 where exists (select 1 from mt_role_permission x where x.mt_role_id = t0.id)");
  }

  @Test
  public void isNotNull_when_ManyToMany_expect_existsSubqueryAndNoJoin() {
    ResetBasicData.reset();

    Query<Role> query = DB.find(Role.class).where().isNotNull("permissions").query();
    query.findList();

    assertThat(query.getGeneratedSql()).contains("from mt_role t0 where exists (select 1 from mt_role_permission x where x.mt_role_id = t0.id)");
  }

}

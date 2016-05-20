package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import com.avaje.tests.model.m2m.Role;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestQueryIsNull extends BaseTestCase {


  @Test
  public void queryShouldContainIsNullOnColumn() {
    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class).where().isNull("customerName").query();
    query.findList();

    assertTrue(query.getGeneratedSql().contains("name is null"));
  }

  @Test
  public void isNotNull_when_OneToMany_expect_existsSubquery() {
    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class).where().isNotNull("details").query();
    query.findList();

    assertTrue(query.getGeneratedSql().contains(" where exists (select 1 from o_order_detail where order_id = t0.id)"));
  }

  @Test
  public void isNotEmpty_when_OneToMany_expect_existsSubquery() {
    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class).where().isNotEmpty("details").query();
    query.findList();

    assertTrue(query.getGeneratedSql().contains(" where exists (select 1 from o_order_detail where order_id = t0.id)"));
  }

  @Test
  public void isNull_when_OneToMany_expect_notExistsSubquery() {
    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class).where().isNull("details").query();
    query.findList();

    assertTrue(query.getGeneratedSql().contains(" where not exists (select 1 from o_order_detail where order_id = t0.id)"));
  }

  @Test
  public void isEmpty_when_OneToMany_expect_notExistsSubquery() {
    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class).where().isEmpty("details").query();
    query.findList();

    assertTrue(query.getGeneratedSql().contains(" where not exists (select 1 from o_order_detail where order_id = t0.id)"));
  }

  @Test
  public void isEmpty_when_ManyToMany_expect_notExistsSubqueryAndNoJoin() {
    ResetBasicData.reset();

    Query<Role> query = Ebean.find(Role.class).where().isEmpty("permissions").query();
    query.findList();

    assertTrue(query.getGeneratedSql().contains("from mt_role t0 where not exists (select 1 from mt_role_permission where mt_role_id = t0.id)"));
  }

  @Test
  public void isNull_when_ManyToMany_expect_notExistsSubqueryAndNoJoin() {
    ResetBasicData.reset();

    Query<Role> query = Ebean.find(Role.class).where().isNull("permissions").query();
    query.findList();

    assertTrue(query.getGeneratedSql().contains("from mt_role t0 where not exists (select 1 from mt_role_permission where mt_role_id = t0.id)"));
  }

  @Test
  public void isNotEmpty_when_ManyToMany_expect_existsSubqueryAndNoJoin() {
    ResetBasicData.reset();

    Query<Role> query = Ebean.find(Role.class).where().isNotEmpty("permissions").query();
    query.findList();

    assertTrue(query.getGeneratedSql().contains("from mt_role t0 where exists (select 1 from mt_role_permission where mt_role_id = t0.id)"));
  }

  @Test
  public void isNotNull_when_ManyToMany_expect_existsSubqueryAndNoJoin() {
    ResetBasicData.reset();

    Query<Role> query = Ebean.find(Role.class).where().isNotNull("permissions").query();
    query.findList();

    assertTrue(query.getGeneratedSql().contains("from mt_role t0 where exists (select 1 from mt_role_permission where mt_role_id = t0.id)"));
  }

}

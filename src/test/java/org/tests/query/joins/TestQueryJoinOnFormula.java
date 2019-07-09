package org.tests.query.joins;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.FetchConfig;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.family.ParentPerson;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;

public class TestQueryJoinOnFormula extends BaseTestCase {

  
  @Before
  public void init() {
    ResetBasicData.reset();
  }
  
  /**
   * If there is no query.select() or query.fetch() in the query, there should be a meaningful exception.
   */
  @Test
  public void test_OrderFindIdsNoSelect() {

    assertThatThrownBy(() -> {
      Ebean.find(Order.class)
        .where().eq("totalItems", 3)
        .findIds();
    }).hasMessageContaining("property 'totalItems' has to be selected explicitly");
  }
  
  /**
   * If there is no query.select() or query.fetch() in the query, there should be a meaningful exception.
   */
  @Test
  public void test_OrderFindListNoSelect() {

    assertThatThrownBy(() -> {
      Ebean.find(Order.class)
        .where().eq("totalItems", 3)
        .findList();
    }).hasMessageContaining("property 'totalItems' has to be selected explicitly");
  }
  
  @Test
  public void test_OrderFindIds() {

    LoggedSqlCollector.start();

    List<Integer> orderIds = Ebean.find(Order.class)
        .select("totalItems")
        .where().eq("totalItems", 3)
        .findIds();
    assertThat(orderIds).hasSize(2);
    
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }
  
  @Test
  public void test_OrderFindList() {

    LoggedSqlCollector.start();

    List<Order> orders = Ebean.find(Order.class)
        .select("totalItems")
        .where().eq("totalItems", 3)
        .findList();
    assertThat(orders).hasSize(2);
    
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }
  
  @Test
  public void test_OrderFindCount() {

    LoggedSqlCollector.start();

    int orders = Ebean.find(Order.class)
        .select("totalItems")
        .where().eq("totalItems", 3)
        .findCount();
    assertThat(orders).isEqualTo(2);
    
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }

  @Test
  public void test_OrderFindSingleAttributeList() {

    LoggedSqlCollector.start();

    List<Date> orderDates = Ebean.find(Order.class)
        .select("orderDate,totalItems")
        .where().eq("totalItems", 3)
        .findSingleAttributeList();
    assertThat(orderDates).hasSize(2);
    
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }
  
  @Test
  public void test_OrderFindOne() {

    LoggedSqlCollector.start();

    Order order = Ebean.find(Order.class)
        .select("totalItems")
        .where().eq("totalItems", 3)
        .setMaxRows(1)
        .orderById(true)
        .findOne();
    
    assertThat(order.getTotalItems()).isEqualTo(3);
    
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }
  
  @Test
  public void test_ParentPersonFindIds() {

    LoggedSqlCollector.start();

    List<ParentPerson> orderIds = Ebean.find(ParentPerson.class)
        .where().eq("totalAge", 3)
        .findIds();
    assertThat(orderIds).hasSize(2);
    
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }
  
  @Test
  public void test_ParentPersonFindList() {

    LoggedSqlCollector.start();

    Ebean.find(ParentPerson.class)
        .where().eq("totalAge", 3)
        .findList();
    // TODO: There are no beans in database, so for now only the query must run.
    
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }
  
  @Test
  public void test_ParentPersonFindCount() {

    LoggedSqlCollector.start();

    Ebean.find(ParentPerson.class)
      .where().eq("totalAge", 3)
      .findCount();
    // TODO: There are no beans in database, so for now only the query must run.
    
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }
  
  @Test
  public void test_ParentPersonFindSingleAttributeList() {

    LoggedSqlCollector.start();

    Ebean.find(ParentPerson.class)
      .select("address") // .select("address, totalAge") would work
      .where().eq("totalAge", 3)
      .findSingleAttributeList();
    // TODO: There are no beans in database, so for now only the query must run.
    
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }
  
  @Test
  public void test_ParentPersonFindOne() {

    LoggedSqlCollector.start();

    Ebean.find(ParentPerson.class)
      .where().eq("totalAge", 3)
      .setMaxRows(1)
      .orderById(true)
      .findOne();
    // TODO: There are no beans in database, so for now only the query must run.
    
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertEquals(1, loggedSql.size());
  }
}

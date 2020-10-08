package io.ebeaninternal.server.expression;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.ExpressionList;
import io.ebeaninternal.api.SpiExpression;
import org.tests.model.basic.Order;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PrepareDocNestedTest extends BaseTestCase {
  @Test
  public void prepare() throws Exception {

    ExpressionList<Order> where = Ebean.find(Order.class)
      .where()
      .gt("details.orderQty", 1)
      .query().where();


    DefaultExpressionList<?> exp = (DefaultExpressionList<?>) where;
    PrepareDocNested.prepare(exp, getBeanDescriptor(Order.class));

    List<SpiExpression> underlyingList = exp.getUnderlyingList();
    assertEquals(underlyingList.size(), 1);
    assertEquals(exp.allDocNestedPath, "details");
  }

  @Test
  public void prepare_when_multipleOfSamePath() throws Exception {

    ExpressionList<Order> where = Ebean.find(Order.class)
      .where()
      .gt("details.orderQty", 1)
      .gt("details.unitPrice", 1)
      .query().where();


    DefaultExpressionList<?> exp = (DefaultExpressionList<?>) where;
    PrepareDocNested.prepare(exp, getBeanDescriptor(Order.class));

    List<SpiExpression> underlyingList = exp.getUnderlyingList();
    assertEquals(underlyingList.size(), 2);
    assertEquals(exp.allDocNestedPath, "details");
  }

  @Test
  public void prepare_when_mixed() throws Exception {

    ExpressionList<Order> where = Ebean.find(Order.class)
      .where()
      .gt("customer.id", 1)
      .gt("details.orderQty", 1)
      .gt("details.unitPrice", 1)
      .query().where();


    DefaultExpressionList<?> exp = (DefaultExpressionList<?>) where;
    PrepareDocNested.prepare(exp, getBeanDescriptor(Order.class));

    List<SpiExpression> underlyingList = exp.getUnderlyingList();
    assertEquals(underlyingList.size(), 2);
    assertNull(exp.allDocNestedPath);

    DefaultExpressionList<?> second = (DefaultExpressionList<?>) underlyingList.get(1);
    assertEquals(second.allDocNestedPath, "details");
  }

  @Test
  public void prepare_when_nestedJunction() throws Exception {

    ExpressionList<Order> where = Ebean.find(Order.class)
      .where()
      .not()
      .gt("customer.id", 1)
      .gt("details.orderQty", 1)
      .gt("details.unitPrice", 1)
      .query().where();

    DefaultExpressionList<?> exp = (DefaultExpressionList<?>) where;
    PrepareDocNested.prepare(exp, getBeanDescriptor(Order.class));

    List<SpiExpression> underlyingList = exp.getUnderlyingList();
    assertEquals(underlyingList.size(), 1);
    assertNull(exp.allDocNestedPath);

    JunctionExpression<?> junction = (JunctionExpression<?>) underlyingList.get(0);
    List<SpiExpression> junctionUnderlying = junction.exprList.getUnderlyingList();
    JunctionExpression<?> nestedNestedPath = (JunctionExpression<?>) junctionUnderlying.get(1);
    assertEquals(nestedNestedPath.exprList.allDocNestedPath, "details");
  }

  @Test
  public void prepare_when_nestedMultiple() throws Exception {

    ExpressionList<Order> where = Ebean.find(Order.class)
      .where()
      .isNotNull("shipments.shipTime")
      .gt("details.orderQty", 1)
      .gt("details.unitPrice", 1)
      .query().where();

    DefaultExpressionList<?> exp = (DefaultExpressionList<?>) where;
    PrepareDocNested.prepare(exp, getBeanDescriptor(Order.class));

    List<SpiExpression> underlyingList = exp.getUnderlyingList();
    assertEquals(underlyingList.size(), 2);
    assertNull(exp.allDocNestedPath);

    DefaultExpressionList<?> shipExpr = (DefaultExpressionList<?>) underlyingList.get(0);
    assertEquals(shipExpr.allDocNestedPath, "shipments");

    DefaultExpressionList<?> detailsExpr = (DefaultExpressionList<?>) underlyingList.get(1);
    assertEquals(detailsExpr.allDocNestedPath, "details");
  }


  @Test
  public void prepare_when_manyMixed() throws Exception {

    ExpressionList<Order> where = Ebean.find(Order.class)
      .where()
      .gt("customer.id", 1)             // 0
      .isNotNull("shipments.shipTime")  // shipments 0
      .isNotNull("status")              // 1
      .gt("details.orderQty", 1)        // details 0
      .isNotNull("orderDate")           // 2
      .gt("details.unitPrice", 1)       // details 1
      .query().where();

    DefaultExpressionList<?> exp = (DefaultExpressionList<?>) where;
    PrepareDocNested.prepare(exp, getBeanDescriptor(Order.class));

    List<SpiExpression> underlyingList = exp.getUnderlyingList();
    assertEquals(underlyingList.size(), 5);
    assertNull(exp.allDocNestedPath);

    DefaultExpressionList<?> shipExpr = (DefaultExpressionList<?>) underlyingList.get(3);
    assertEquals(shipExpr.allDocNestedPath, "shipments");

    DefaultExpressionList<?> detailsExpr = (DefaultExpressionList<?>) underlyingList.get(4);
    assertEquals(detailsExpr.allDocNestedPath, "details");
  }
}

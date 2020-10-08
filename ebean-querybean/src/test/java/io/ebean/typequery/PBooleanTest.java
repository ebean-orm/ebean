package io.ebean.typequery;

import io.ebeaninternal.server.expression.DefaultExpressionList;
import io.ebeaninternal.server.expression.SimpleExpression;
import org.example.domain.Customer;
import org.example.domain.query.QCustomer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PBooleanTest {

  PBoolean<QCustomer> property(QCustomer customer) {
    return new PBoolean<>("active", customer);
  }

  @Test
  public void constructWithPrefix() throws Exception {

    QCustomer customer = new QCustomer();
    PBoolean<QCustomer> property = new PBoolean<>("active", customer, null);
    property.isTrue();

    assertThat(getExpression(customer).getValue()).isEqualTo(Boolean.TRUE);
  }
  @Test
  public void isTrue() throws Exception {

    QCustomer customer = new QCustomer();
    PBoolean property = property(customer);
    property.isTrue();

    assertThat(getExpression(customer).getValue()).isEqualTo(Boolean.TRUE);
  }

  @Test
  public void isFalse() throws Exception {

    QCustomer customer = new QCustomer();
    PBoolean property = property(customer);
    property.isFalse();

    assertThat(getExpression(customer).getValue()).isEqualTo(Boolean.FALSE);
  }

  @Test
  public void is_false() throws Exception {

    QCustomer customer = new QCustomer();
    PBoolean property = property(customer);
    property.is(false);

    assertThat(getExpression(customer).getValue()).isEqualTo(Boolean.FALSE);
  }

  @Test
  public void is_true() throws Exception {

    QCustomer customer = new QCustomer();
    PBoolean property = property(customer);
    property.is(true);

    assertThat(getExpression(customer).getValue()).isEqualTo(Boolean.TRUE);
  }

  @Test
  public void eq_true() throws Exception {

    QCustomer customer = new QCustomer();
    PBoolean property = property(customer);
    property.eq(true);

    assertThat(getExpression(customer).getValue()).isEqualTo(Boolean.TRUE);
  }

  @Test
  public void eq_false() throws Exception {

    QCustomer customer = new QCustomer();
    PBoolean property = property(customer);
    property.eq(false);

    assertThat(getExpression(customer).getValue()).isEqualTo(Boolean.FALSE);
  }

  private SimpleExpression getExpression(QCustomer customer) {
    DefaultExpressionList<Customer> where = (DefaultExpressionList<Customer>)customer.query().where();
    return (SimpleExpression)where.getUnderlyingList().get(0);
  }


}

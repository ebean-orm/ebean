package io.ebeaninternal.server.expression;

import io.ebean.LikeType;
import io.ebean.bean.EntityBean;
import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Customer;


public class DefaultExampleExpressionTest extends BaseExpressionTest {


  Customer customer() {
    Address address = new Address();
    address.setCity("billingAddress.city");

    Customer customer = new Customer();
    customer.setName("name");
    customer.setBillingAddress(address);
    return customer;
  }

  DefaultExampleExpression exp() {
    Customer customer = customer();
    return new DefaultExampleExpression((EntityBean) customer, false, LikeType.EQUAL_TO);
  }

  DefaultExampleExpression expExtra() {
    Customer customer = customer();
    customer.setSmallnote("smallNote");
    return new DefaultExampleExpression((EntityBean) customer, false, LikeType.EQUAL_TO);
  }

  DefaultExampleExpression expDiffName() {
    Customer customer = customer();
    customer.setName("otherName");
    return new DefaultExampleExpression((EntityBean) customer, false, LikeType.EQUAL_TO);
  }

  BeanDescriptor<Customer> customerBeanDescriptor() {
    return getBeanDescriptor(Customer.class);
  }

  DefaultExampleExpression prepare(DefaultExampleExpression expr) {

    SpiQuery<Customer> query = (SpiQuery<Customer>) spiEbeanServer().find(Customer.class);
    BeanQueryRequest<?> request = create(query);
    expr.containsMany(customerBeanDescriptor(), new ManyWhereJoins());
    expr.prepareExpression(request);
    return expr;
  }

  @Test
  public void test() {

    DefaultExampleExpression expr = exp();

    prepare(expr);

    StringBuilder builder = new StringBuilder();
    expr.queryPlanHash(builder);

    TDSpiExpressionRequest req = new TDSpiExpressionRequest(customerBeanDescriptor());
    expr.addBindValues(req);

    Assertions.assertThat(req.bindValues).contains("name", "billingAddress.city");
  }


  private <T> OrmQueryRequest<T> create(SpiQuery<T> query) {
    return new OrmQueryRequest<>(null, null, query, null);
  }

  @Test
  public void isSameByPlan_whenSame() {

    same(prepare(exp()), prepare(exp()));
  }

  @Test
  public void isSameByPlan_when_diffBindValue_stillSame() {

    same(prepare(exp()), prepare(expDiffName()));
  }

  @Test
  public void isSameByPlan_when_extraExpression_then_different() {

    different(prepare(exp()), prepare(expExtra()));
  }

  @Test
  public void isSameByPlan_when_lessExpression_then_different() {

    different(prepare(expExtra()), prepare(exp()));
  }

}

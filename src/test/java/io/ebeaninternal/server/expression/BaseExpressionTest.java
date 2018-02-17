package io.ebeaninternal.server.expression;

import io.ebean.BaseTestCase;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;

import static org.assertj.core.api.StrictAssertions.assertThat;

public abstract class BaseExpressionTest extends BaseTestCase {

  protected DefaultExpressionRequest newExpressionRequest() {
    BeanDescriptor<Order> desc = getBeanDescriptor(Order.class);
    return new DefaultExpressionRequest(desc);
  }

  protected String hash(SpiExpression expression) {
    StringBuilder sb  = new StringBuilder();
    if (expression != null) {
      expression.queryPlanHash(sb);
    }
    return sb.toString();
  }

  protected void same(SpiExpression one, SpiExpression two){
    assertThat(hash(one)).isEqualTo(hash(two));
  }

  protected void different(SpiExpression one, SpiExpression two){
    assertThat(hash(one)).isNotEqualTo(hash(two));
  }

  /**
   * Request with Multi-Value support.
   */
  protected InExpressionTest.TDQueryRequest<Customer> multi() {
    return MULTI_VALUE;
  }

  /**
   * Request with NO Multi-Value support.
   */
  protected InExpressionTest.TDQueryRequest<Customer> noMulti() {
    return NO_MULTI_VALUE;
  }


  private static final TDQueryRequest<Customer> MULTI_VALUE= new TDQueryRequest<>(true);
  private static final TDQueryRequest<Customer> NO_MULTI_VALUE = new TDQueryRequest<>(false);

  static class TDQueryRequest<T> implements BeanQueryRequest<T> {

    final boolean supported;

    TDQueryRequest(boolean supported) {
      this.supported = supported;
    }

    @Override
    public EbeanServer getEbeanServer() {
      return null;
    }

    @Override
    public Transaction getTransaction() {
      return null;
    }

    @Override
    public Query<T> getQuery() {
      return null;
    }

    @Override
    public boolean isMultiValueIdSupported() {
      return supported;
    }

    @Override
    public boolean isMultiValueSupported(Class<?> valueType) {
      return supported;
    }
  }
}

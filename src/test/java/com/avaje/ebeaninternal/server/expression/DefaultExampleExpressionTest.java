package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.LikeType;
import com.avaje.ebean.Query;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class DefaultExampleExpressionTest extends BaseTestCase {


  @Test
  public void test() {

    SpiEbeanServer server = (SpiEbeanServer)Ebean.getDefaultServer();
    BeanDescriptor<Customer> desc = server.getBeanDescriptor(Customer.class);

    SpiQuery<Customer> query = (SpiQuery<Customer>)server.find(Customer.class);

    Address address = new Address();
    address.setCity("billingAddress.city");

    Customer customer = new Customer();
    customer.setName("name");
    customer.setBillingAddress(address);


    DefaultExampleExpression expr = new DefaultExampleExpression((EntityBean)customer, false, LikeType.EQUAL_TO);


    BeanQueryRequest<?> request = create(query, desc);
    HashQueryPlanBuilder builder = new HashQueryPlanBuilder();
    expr.queryPlanHash(request, builder);

    TDSpiExpressionRequest req = new TDSpiExpressionRequest(desc);
    expr.addBindValues(req);

    assertThat(req.bindValues).contains("name", "billingAddress.city");

    address.setCity("Auckland");
    customer.setName("Rob");

    ResetBasicData.reset();

    Query<Customer> query1 = server.find(Customer.class)
        .where().exampleLike(customer)
        .query();

    query1.findList();

    assertThat(query1.getGeneratedSql()).contains("(t0.name like ? ");
    assertThat(query1.getGeneratedSql()).contains(" and t1.city like ? ");

  }

  private <T> OrmQueryRequest<T> create(SpiQuery<T> query, BeanDescriptor<T> desc) {
    return new OrmQueryRequest<T>(null, null, query, desc, null);
  }

}
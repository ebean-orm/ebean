package io.ebean.plugin;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.FetchPath;
import io.ebean.Query;
import io.ebean.text.PathProperties;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import org.junit.Test;
import org.tests.inheritance.Stockforecast;
import org.tests.model.basic.Car;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.Person;
import org.tests.model.basic.Product;
import org.tests.model.basic.Vehicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class BeanTypeTest {

  private static Database db = DB.getDefault();

  private <T> BeanType<T> beanType(Class<T> cls) {
    return db.getPluginApi().getBeanType(cls);
  }

  @Test
  public void getBeanType() {
    assertThat(beanType(Order.class).getBeanType()).isEqualTo(Order.class);
  }

  @Test
  public void getTypeAtPath_when_ManyToOne() {
    BeanType<Order> orderType = beanType(Order.class);
    BeanType<?> customerType = orderType.getBeanTypeAtPath("customer");
    assertThat(customerType.getBeanType()).isEqualTo(Customer.class);
  }

  @Test
  public void getTypeAtPath_when_OneToMany() {
    BeanType<Order> orderType = beanType(Order.class);
    BeanType<?> detailsType = orderType.getBeanTypeAtPath("details");
    assertThat(detailsType.getBeanType()).isEqualTo(OrderDetail.class);
  }

  @Test
  public void getTypeAtPath_when_nested() {
    BeanType<Order> orderType = beanType(Order.class);
    BeanType<?> productType = orderType.getBeanTypeAtPath("details.product");
    assertThat(productType.getBeanType()).isEqualTo(Product.class);
  }

  @Test(expected = RuntimeException.class)
  public void getTypeAtPath_when_simpleType() {

    beanType(Order.class).getBeanTypeAtPath("status");
  }

  @Test
  public void createBean() {

    assertThat(beanType(Order.class).createBean()).isNotNull();
  }

  @Test
  public void property() {

    Order order = new Order();
    order.setStatus(Order.Status.APPROVED);
    Property statusProperty = beanType(Order.class).getProperty("status");

    assertThat(statusProperty.getVal(order)).isEqualTo(order.getStatus());
  }

  @Test
  public void getBaseTable() {

    assertThat(beanType(Order.class).getBaseTable()).isEqualTo("o_order");
  }

  @Test
  public void beanId_and_getBeanId() {

    Order order = new Order();
    order.setId(42);

    Object id1 = beanType(Order.class).beanId(order);
    Object id2 = beanType(Order.class).getBeanId(order);

    assertThat(id1).isEqualTo(order.getId());
    assertThat(id2).isEqualTo(order.getId());
  }

  @Test
  public void setBeanId() {

    Order order = new Order();
    beanType(Order.class).setBeanId(order, 42);

    assertThat(42).isEqualTo(order.getId());
  }

  @Test
  public void isDocStoreIndex() {

    assertThat(beanType(Order.class).isDocStoreMapped()).isFalse();
    assertThat(beanType(Person.class).isDocStoreMapped()).isFalse();

    assertThat(beanType(Order.class).getDocMapping()).isNotNull();
    assertThat(beanType(Person.class).getDocMapping()).isNull();
  }

  @Test
  public void docStore_getEmbedded() {

    BeanDocType<Order> orderDocType = beanType(Order.class).docStore();
    FetchPath customer = orderDocType.getEmbedded("customer");
    assertThat(customer).isNotNull();
    assertThat(customer.getProperties(null)).contains("id", "name");
  }

  @Test
  public void docStore_getEmbeddedManyRoot() {

    BeanDocType<Order> orderDocType = beanType(Order.class).docStore();

    FetchPath detailsPath = orderDocType.getEmbedded("details");
    assertThat(detailsPath).isNotNull();

    FetchPath detailsRoot = orderDocType.getEmbeddedManyRoot("details");
    assertThat(detailsRoot).isNotNull();
    assertThat(detailsRoot.getProperties(null)).containsExactly("id", "details");
    assertThat(detailsRoot.hasPath("details")).isTrue();
  }

  @Test
  public void getDocStoreQueueId() {

    assertThat(beanType(Order.class).getDocStoreQueueId()).isEqualTo("order");
    assertThat(beanType(Customer.class).getDocStoreQueueId()).isEqualTo("customer");
  }

  @Test
  public void getDocStoreIndexType() {

    assertThat(beanType(Order.class).docStore().getIndexType()).isEqualTo("order");
    assertThat(beanType(Customer.class).docStore().getIndexType()).isEqualTo("customer");
  }

  @Test
  public void getDocStoreIndexName() {

    assertThat(beanType(Order.class).docStore().getIndexType()).isEqualTo("order");
    assertThat(beanType(Customer.class).docStore().getIndexType()).isEqualTo("customer");
  }

  @Test
  public void docStoreNested() {

    FetchPath parse = PathProperties.parse("id,name");

    FetchPath nestedCustomer = beanType(Order.class).docStore().getEmbedded("customer");
    assertThat(nestedCustomer.toString()).isEqualTo(parse.toString());
  }

  @Test
  public void docStoreApplyPath() {

    SpiQuery<Order> orderQuery = (SpiQuery<Order>) db.find(Order.class);
    beanType(Order.class).docStore().applyPath(orderQuery);

    OrmQueryDetail detail = orderQuery.getDetail();
    assertThat(detail.getChunk("customer", false).getIncluded()).containsExactly("id", "name");
  }

  @Test(expected = IllegalStateException.class)
  public void docStoreIndex() throws Exception {
    beanType(Order.class).docStore().index(1, new Order(), null);
  }

  @Test(expected = IllegalStateException.class)
  public void docStoreDeleteById() throws Exception {
    beanType(Order.class).docStore().deleteById(1, null);
  }

  @Test(expected = IllegalStateException.class)
  public void docStoreUpdateEmbedded() throws Exception {
    beanType(Order.class).docStore().updateEmbedded(1, "customer", "someJson", null);
  }

  @Test
  public void hasInheritance_when_not() {
    assertFalse(beanType(Order.class).hasInheritance());
  }

  @Test
  public void hasInheritance_when_root() {
    assertTrue(beanType(Vehicle.class).hasInheritance());
  }

  @Test
  public void hasInheritance_when_leaf() {
    assertTrue(beanType(Car.class).hasInheritance());
  }

  @Test
  public void getDiscColumn_when_default() {
    assertEquals(beanType(Car.class).getDiscColumn(), "dtype");
  }

  @Test
  public void getDiscColumn_when_set() {
    assertEquals(beanType(Stockforecast.class).getDiscColumn(), "type");
  }

  @Test
  public void createBeanUsingDisc_when_set() {
    Vehicle vehicle = beanType(Vehicle.class).createBeanUsingDisc("C");
    assertTrue(vehicle instanceof Car);
  }

  @Test
  public void addInheritanceWhere_when_leaf() {
    Query<Vehicle> query = db.find(Vehicle.class);
    beanType(Car.class).addInheritanceWhere(query);
  }

  @Test
  public void addInheritanceWhere_when_root() {
    Query<Vehicle> query = db.find(Vehicle.class);
    beanType(Vehicle.class).addInheritanceWhere(query);
  }

}

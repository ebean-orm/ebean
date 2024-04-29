package org.tests.docstore;


import io.ebean.BeanState;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.text.json.JsonReadOptions;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Product;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.docstore.CustomerReport;
import org.tests.model.docstore.ProductReport;
import org.tests.model.docstore.ReportContainer;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerReportTest extends BaseTestCase {

  @Test
  public void testToJson() throws Exception {
    ResetBasicData.reset();


    String json = server().json().toJson(getCustomerReport());
    assertThat(json).isEqualTo("{\"dtype\":\"CR\",\"friends\":[{\"id\":2},{\"id\":3}],\"customer\":{\"id\":1}}");
  }

  @Test
  public void testFromJson() throws Exception {
    ResetBasicData.reset();
    String json = "{\"dtype\":\"CR\",\"friends\":[{\"id\":2},{\"id\":3}],\"customer\":{\"id\":1}}";

    JsonReadOptions opts = new JsonReadOptions();
    opts.setEnableLazyLoading(true);
    CustomerReport report = server().json().toBean(CustomerReport.class, json, opts);

    assertThat(report.getCustomer().getName()).isEqualTo("Rob");

    assertThat(report.getFriends().get(0).getName()).isEqualTo("Cust NoAddress");
    assertThat(report.getFriends().get(1).getName()).isEqualTo("Fiona");
  }


  @Test
  public void testEmbeddedDocs() throws Exception {
    ResetBasicData.reset();

    CustomerReport report = getCustomerReport();
    report.getEmbeddedReports().add(getProductReport());

    String json = server().json().toJson(report);

    assertThat(json).isEqualTo("{\"dtype\":\"CR\","
      + "\"embeddedReports\":[{\"dtype\":\"PR\",\"title\":\"This is a good product\",\"product\":{\"id\":1}}],"
      + "\"friends\":[{\"id\":2},{\"id\":3}],"
      + "\"customer\":{\"id\":1}}");

    JsonReadOptions opts = new JsonReadOptions();
    opts.setEnableLazyLoading(true);
    report = server().json().toBean(CustomerReport.class, json, opts);
    ProductReport ar = (ProductReport) report.getEmbeddedReports().get(0);
    assertThat(ar.getTitle()).isEqualTo("This is a good product");
    assertThat(ar.getProduct().getName()).isEqualTo("Chair");
  }


  private CustomerReport getCustomerReport() {
    Customer customer = server().reference(Customer.class, 1);
    Customer friend1 = server().reference(Customer.class, 2);
    Customer friend2 = server().reference(Customer.class, 3);

    CustomerReport report = new CustomerReport();

    report.setCustomer(customer);
    report.setFriends(Arrays.asList(friend1, friend2));
    return report;
  }

  private ProductReport getProductReport() {
    Product product = server().reference(Product.class, 1);

    ProductReport report = new ProductReport();
    report.setTitle("This is a good product");
    report.setProduct(product);
    return report;
  }

  /**
   * Tests the inheritance support for DocStore/Jsons.
   * We do not use default jackson serialization. In Report-class
   * there are (de)serializers, that will delegate the (de)serialization
   * back to ebean.
   * <p>
   * Big advantage: Ebean supports Inheritance with JSONS and some kind
   * of "autodiscovery".
   * <p>
   * In theory, Jackson could do serialization with `@JsonSubTypes`, but
   * they have to be specified in the top class. See
   * https://github.com/FasterXML/jackson-databind/issues/2104
   */
  @Test
  public void testInheritance() {
    ReportContainer container = new ReportContainer();
    container.setReport(new ProductReport());
    DB.save(container);

    ReportContainer container2 = new ReportContainer();
    container2.setReport(new CustomerReport());
    DB.save(container2);

    container = DB.find(ReportContainer.class, container.getId());
    container2 = DB.find(ReportContainer.class, container2.getId());

    assertThat(container.getReport()).isInstanceOf(ProductReport.class);
    assertThat(container2.getReport()).isInstanceOf(CustomerReport.class);
  }

  /**
   * This test shows how we do the (de)serialization when we reference entites, that are persisted in the DB
   */
  @Test
  public void testReferenceBean() {
    ResetBasicData.reset();
    ReportContainer container = new ReportContainer();
    CustomerReport report = new CustomerReport();
    container.setReport(report);

    Object robId = DB.find(Customer.class).where().eq("name", "Rob").findIds().get(0);
    report.setFriends(DB.find(Customer.class).where().eq("name", "Fiona").findList());
    LoggedSql.start();
    report.setCustomer(DB.reference(Customer.class, robId));
    DB.save(container);
    assertThat(LoggedSql.stop()).hasSize(1); // no lazy load

    container = DB.find(ReportContainer.class, container.getId());
    assertThat(((CustomerReport) container.getReport()).getCustomer().getName()).isEqualTo("Rob");
    assertThat(((CustomerReport) container.getReport()).getFriends().get(0).getName()).isEqualTo("Fiona");
  }

  /**
   * This test shows the dirty detection on docstore beans.
   */
  @Test
  public void testJsonBeanState() {
    ResetBasicData.reset();
    ReportContainer container = new ReportContainer();
    CustomerReport report = new CustomerReport();
    report.setTitle("Foo");
    container.setReport(report);

    BeanState state = DB.beanState(container.getReport());
    assertThat(state.isNew()).isTrue();

    DB.save(container);
    container = DB.find(ReportContainer.class, container.getId());

     state = DB.beanState(container.getReport());
    assertThat(state.isDirty()).isFalse();
    assertThat(state.isNew()).isFalse(); // this detection does not work on JSONs

    container.getReport().setTitle("Bar");
    state = DB.beanState(container.getReport());
    assertThat(state.isDirty()).isTrue();
    //BTW: ValuePair has no equals & hashcode
    //assertThat(state.dirtyValues()).hasSize(1).containsEntry("title", new ValuePair("Bar", "Foo"));
    assertThat(state.dirtyValues()).hasSize(1).hasToString("{title=Bar,Foo}");
  }
}

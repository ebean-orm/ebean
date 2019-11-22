package org.tests.docstore;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Product;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.docstore.CustomerReport;
import org.tests.model.docstore.ProductReport;
import org.tests.model.docstore.Report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.ebean.BaseTestCase;
import io.ebean.plugin.BeanType;
import io.ebean.text.json.JsonReadOptions;
import io.ebean.text.json.JsonVersionMigrationHandler;
import io.ebean.text.json.JsonWriteOptions;

public class CustomerReportTest extends BaseTestCase {

  private static class MigHandler implements JsonVersionMigrationHandler {

    @Override
    public ObjectNode migrateRoot(ObjectNode node, ObjectMapper mapper, BeanType<?> rootBeanType) throws IOException {

      int version = node.get("_bv") == null ? 1 : node.get("_bv").asInt();

      if (version == 2) {
        if ("CustomerReport".equals(node.get("dtype").asText())) {
          node.put("dtype", "CR");
        }
        version = 3;
      }
      node.put("_bv", version);

      return node;
    }

    @Override
    public ObjectNode migrate(ObjectNode node, ObjectMapper mapper, BeanType<?> beanType) throws IOException {
      return node;
    }

  }

  @Test
  public void testToJson() throws Exception {
    ResetBasicData.reset();



    String json = server().json().toJson(getCustomerReport());


    assertThat(json).isEqualTo("{\"dtype\":\"CR\",\"friends\":[{\"id\":2},{\"id\":3}],\"customer\":{\"id\":1}}");

    JsonWriteOptions opts = new JsonWriteOptions();
    opts.setVersionWriter((writer, desc) -> {
      int version = 0;
      if (Report.class.isAssignableFrom(desc.getBeanType())) {
        version = 3;
      }
      writer.writeNumberField("_bv", version);
    });
    json = server().json().toJson(getCustomerReport(),opts);
    assertThat(json).isEqualTo("{\"_bv\":3,\"dtype\":\"CR\",\"friends\":[{\"_bv\":0,\"id\":2},{\"_bv\":0,\"id\":3}],\"customer\":{\"_bv\":0,\"id\":1}}");
  }

  @Test
  public void testMigration() {
    String json = "{\"_bv\":2,\"dtype\":\"CustomerReport\",\"friends\":[{\"_bv\":0,\"id\":2},{\"_bv\":0,\"id\":3}],\"customer\":{\"_bv\":0,\"id\":1}}";
    JsonReadOptions readOpts = new JsonReadOptions();
    readOpts.setVersionMigrationHandler(new MigHandler());
    Report report = server().json().toBean(Report.class, json, readOpts);
    assertThat(report).isInstanceOf(CustomerReport.class);
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
    Customer customer = server().getReference(Customer.class, 1);
    Customer friend1 = server().getReference(Customer.class, 2);
    Customer friend2 = server().getReference(Customer.class, 3);

    CustomerReport report = new CustomerReport();

    report.setCustomer(customer);
    report.setFriends(Arrays.asList(friend1, friend2));
    return report;
  }

  private ProductReport getProductReport() {
    Product product = server().getReference(Product.class, 1);

    ProductReport report = new ProductReport();
    report.setTitle("This is a good product");
    report.setProduct(product);
    return report;
  }
}

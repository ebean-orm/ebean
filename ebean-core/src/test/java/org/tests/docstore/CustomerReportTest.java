package org.tests.docstore;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Product;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.docstore.CustomerReport;
import org.tests.model.docstore.ProductReport;

import io.ebean.BaseTestCase;
import io.ebean.text.json.JsonReadOptions;

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

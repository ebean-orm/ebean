package org.tests.text.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.text.json.JsonWriteOptions;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

public class TestTextJsonForceReference extends BaseTestCase {

  @Test
  public void testWithRefBean() throws IOException {

    ResetBasicData.reset();

    Customer cust = Ebean.find(Customer.class).where().eq("name", "Rob").findOne();
    JsonWriteOptions options = JsonWriteOptions.parsePath("*");

    String json1 = Ebean.json().toJson(cust, options); // contains "billingAddress":{"id":1}

    cust.getBillingAddress().getCity(); // load the bean and convert to again

    String json2 = Ebean.json().toJson(cust, options); // contains whole billing address.
    // this test shows that the json result depends on the state of the referenced bean
    assertThat(json1).isNotEqualTo(json2);

  }


  @Test
  public void testWithForceReference() throws IOException {

    ResetBasicData.reset();

    Customer cust = Ebean.find(Customer.class).where().eq("name", "Rob").findOne();
    JsonWriteOptions options = JsonWriteOptions.parsePath("*");
    options.setForceReference(true);

    String json1 = Ebean.json().toJson(cust, options); // contains "billingAddress":{"id":1}

    cust.getBillingAddress().getCity(); // load the bean and convert to again

    String json2 = Ebean.json().toJson(cust, options); // still contains reference.

    assertThat(json1).isEqualTo(json2);

  }
}

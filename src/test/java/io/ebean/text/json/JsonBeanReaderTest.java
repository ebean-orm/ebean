package io.ebean.text.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.bean.PersistenceContext;
import org.tests.model.basic.Customer;
import com.fasterxml.jackson.core.JsonParser;
import org.junit.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;


public class JsonBeanReaderTest extends BaseTestCase {

  static JsonContext json = Ebean.json();

  @Test
  public void read() throws Exception {

    JsonParser parser = getParser();
    JsonBeanReader<Customer> beanReader = json.createBeanReader(Customer.class, parser, null);

    Customer customer = beanReader.read();
    assertThat(customer.getId()).isEqualTo(42);
    assertThat(customer.getName()).isEqualTo("dummy");
  }

  private JsonParser getParser() {

    Customer customer = new Customer();
    customer.setId(42);
    customer.setName("dummy");

    String rawJson = json.toJson(customer);
    StringReader reader = new StringReader(rawJson);

    return json.createParser(reader);
  }

  @Test
  public void forJson() throws Exception {

    JsonParser parser = getParser();
    JsonBeanReader<Customer> beanReader = json.createBeanReader(Customer.class, parser, null);
    beanReader.read();

    JsonParser more = getParser();
    JsonBeanReader<Customer> moreReader = beanReader.forJson(more, true);

    Customer customer = moreReader.read();
    assertThat(customer.getId()).isEqualTo(42);
    assertThat(customer.getName()).isEqualTo("dummy");
  }

  @Test
  public void persistenceContextPut_when_noPC() throws Exception {

    JsonParser parser = getParser();
    JsonBeanReader<Customer> beanReader = json.createBeanReader(Customer.class, parser, null);
    beanReader.read();

    Customer other = new Customer();
    other.setId(54);

    beanReader.persistenceContextPut(54, other);
  }

  @Test
  public void persistenceContextPut_when_hasPC() throws Exception {

    JsonReadOptions options = new JsonReadOptions().setEnableLazyLoading(true);

    JsonParser parser = getParser();
    JsonBeanReader<Customer> beanReader = json.createBeanReader(Customer.class, parser, options);
    Customer customer = beanReader.read();

    Customer other = new Customer();
    other.setId(54);

    beanReader.persistenceContextPut(54, other);
    PersistenceContext pc = beanReader.getPersistenceContext();
    assertThat(pc.get(Customer.class, 54)).isSameAs(other);
    assertThat(pc.get(Customer.class, 42)).isSameAs(customer);
  }

}

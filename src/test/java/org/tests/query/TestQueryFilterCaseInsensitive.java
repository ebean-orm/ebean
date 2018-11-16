package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryFilterCaseInsensitive extends BaseTestCase {

  @BeforeClass
  public static void setup() {
    ResetBasicData.reset();
  }

  @Test
  public void testEq() {

    // Note: this test uses only customer#1..#4

    List<Customer> customers = Ebean.find(Customer.class).where()
        .eq("name", "ROB") // case match
        .le("id", 4).findList();

    assertThat(customers).isEmpty();

    customers = Ebean.find(Customer.class).where()
        .ieq("name", "ROB") // case insensitive match
        .le("id", 4).findList();

    assertThat(customers).hasSize(1);
  }

  @Test
  public void testNe() {
    List<Customer> customers = Ebean.find(Customer.class).where()
        .ne("name", "ROB") // case match
        .le("id", 4).findList();

    assertThat(customers).hasSize(4);

    customers = Ebean.find(Customer.class).where()
        .ine("name", "ROB") // case insensitive match
        .le("id", 4).findList();

    assertThat(customers).hasSize(3);

  }

  @Test
  public void testLike() {
    List<Customer> customers = Ebean.find(Customer.class).where()
        .like("name", "%O%") // case match
        .le("id", 4).findList();

    assertThat(customers).isEmpty();

    customers = Ebean.find(Customer.class).where()
        .ilike("name", "%O%") // case insensitive match
        .le("id", 4).findList();

    assertThat(customers).hasSize(4); // Rob / Fiona / Cust No address / NocCust
  }

  @Test
  public void testContains() {
    List<Customer> customers = Ebean.find(Customer.class).where()
        .contains("name", "O") // case match
        .le("id", 4).findList();

    assertThat(customers).isEmpty();

    customers = Ebean.find(Customer.class).where()
        .icontains("name", "O") // case insensitive match
        .le("id", 4).findList();

    assertThat(customers).hasSize(4); // Rob / Fiona / Cust No address / NocCust

  }

  @Test
  public void testStartsWith() {
    List<Customer> customers = Ebean.find(Customer.class).where()
        .startsWith("name", "RO") // case match
        .le("id", 4).findList();

    assertThat(customers).isEmpty();

    customers = Ebean.find(Customer.class).where()
        .istartsWith("name", "RO") // case insensitive match
        .le("id", 4).findList();

    assertThat(customers).hasSize(1);
  }

  @Test
  public void testEndsWith() {
    List<Customer> customers = Ebean.find(Customer.class).where()
        .endsWith("name", "OB") // case match
        .le("id", 4).findList();

    assertThat(customers).isEmpty();

    customers = Ebean.find(Customer.class).where()
        .iendsWith("name", "OB") // case insensitive match
        .le("id", 4).findList();

    assertThat(customers).hasSize(1);
  }

}

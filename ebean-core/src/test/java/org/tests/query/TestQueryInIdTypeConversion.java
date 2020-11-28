package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryInIdTypeConversion extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class).where().idIn("1", "2").findList();
    assertThat(list).isNotEmpty();
  }

  @Test
  public void test_emptyList() {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class).where().idIn().findList();
    assertThat(list).isEmpty();
  }

  @Test
  public void test_emptyList_idIn() {
    List<Customer> list = DB.find(Customer.class).where().idIn(new Object[] {}).findList();
    assertThat(list).isEmpty();
  }
}

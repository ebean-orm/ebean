package org.tests.model.m2o;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Transactional;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestManyToOneAsOne extends BaseTestCase {

  @Test
  public void test_when_nonJdbcBatch() {
    runInserts();
  }

  @Transactional(batchSize = 20)
  @Test
  @IgnorePlatform({Platform.SQLSERVER, Platform.ORACLE}) // probably due the use of sequences - Empl has already an ID and Addr refers to it.
  public void test_when_jdbcBatch() {
    runInserts();
  }


  private void runInserts() {

    Addr junk = new Addr();
    junk.setName("junk");
    Ebean.save(junk);

    Empl emp = new Empl();
    emp.setName("My Name");

    Addr address = new Addr();
    address.setName("home");
    emp.getAddresses().add(address);

    // if I do an interim Ebean.save here it works
    //Ebean.save(emp);
    address.setEmployee(emp);
    emp.setDefaultAddress(address);

    Ebean.save(emp);

    assertThat(emp.getDefaultAddress().getId()).isNotNull();
    assertThat(address.getEmployee().getId()).isNotNull();

    Empl foundEmpl = Ebean.find(Empl.class, emp.getId());
    assertThat(foundEmpl.getDefaultAddress().getId()).isEqualTo(address.getId());
  }
}

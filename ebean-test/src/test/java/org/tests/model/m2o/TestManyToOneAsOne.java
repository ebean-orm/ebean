package org.tests.model.m2o;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Transactional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestManyToOneAsOne extends BaseTestCase {

  @Test
  public void test_when_nonJdbcBatch() {
    runInserts();
  }

  @Transactional(batchSize = 20)
  @Test
  @IgnorePlatform({Platform.SQLSERVER, Platform.ORACLE, Platform.HANA}) // probably due the use of sequences - Empl has already an ID and Addr refers to it.
  public void test_when_jdbcBatch() {
    runInserts();
  }


  private void runInserts() {

    Addr junk = new Addr();
    junk.setName("junk");
    DB.save(junk);

    Empl emp = new Empl();
    emp.setName("My Name");

    Addr address = new Addr();
    address.setName("home");
    emp.getAddresses().add(address);

    // if I do an interim DB.save here it works
    //DB.save(emp);
    address.setEmployee(emp);
    emp.setDefaultAddress(address);

    DB.save(emp);

    assertThat(emp.getDefaultAddress().getId()).isNotNull();
    assertThat(address.getEmployee().getId()).isNotNull();

    Empl foundEmpl = DB.find(Empl.class, emp.getId());
    assertThat(foundEmpl.getDefaultAddress().getId()).isEqualTo(address.getId());
  }
}

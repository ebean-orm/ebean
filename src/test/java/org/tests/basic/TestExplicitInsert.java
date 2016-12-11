package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.MyEBasicConfigStartup;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TestExplicitInsert extends BaseTestCase {

  @Test
  public void setId_when_converted() {

    EbeanServer server = Ebean.getDefaultServer();

    Customer cust = new Customer();
    Object returnId = server.setBeanId(cust, "42");

    assertThat(returnId).isEqualTo(42);
    assertThat(cust.getId()).isEqualTo(42);
  }

  @Test
  public void setId_when_correctType() {

    EbeanServer server = Ebean.getDefaultServer();

    Customer customer = new Customer();
    Object returnId = server.setBeanId(customer, 42);

    assertThat(returnId).isEqualTo(42);
    assertThat(customer.getId()).isEqualTo(42);
  }

  @Test
  public void test() throws InterruptedException {

    Thread.sleep(100);

    MyEBasicConfigStartup.resetCounters();

    EBasic b = new EBasic();
    b.setName("exp insert");
    b.setDescription("explicit insert");
    b.setStatus(EBasic.Status.ACTIVE);

    EbeanServer server = Ebean.getDefaultServer();
    server.insert(b);

    Assert.assertNotNull(b.getId());

    Assert.assertEquals(b.getId(), server.getBeanId(b));


    EBasic b2 = server.find(EBasic.class, b.getId());
    b2.setId(null);

    b2.setName("force insert");
    server.insert(b2);

    Assert.assertNotNull(b2.getId());
    Assert.assertTrue(!b.getId().equals(b2.getId()));

    List<EBasic> list = server.find(EBasic.class).where().in("id", b.getId(), b2.getId()).findList();

    assertEquals(2, list.size());

    b2.setName("do an update");
    server.save(b2);

    server.delete(b);
    server.delete(b2);

    // just sleep a little to allow the background thread to fire
    Thread.sleep(100);

    assertEquals(2, MyEBasicConfigStartup.insertCount.get());
    assertEquals(1, MyEBasicConfigStartup.updateCount.get());
    assertEquals(2, MyEBasicConfigStartup.deleteCount.get());
  }

}

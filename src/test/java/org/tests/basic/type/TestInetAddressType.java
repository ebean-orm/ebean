package org.tests.basic.type;

import io.ebean.Ebean;
import io.ebean.TransactionalTestCase;

import org.tests.model.basic.EWithInetAddr;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestInetAddressType extends TransactionalTestCase {

  @Test
  public void testIp4() throws UnknownHostException {

    insertUpdateDeleteFind("120.12.12.56");
  }

  @Test
  public void testIp6() throws UnknownHostException {

    insertUpdateDeleteFind("2001:db8:85a3:0:0:8a2e:370:7334");
  }

  private void insertUpdateDeleteFind(String ipAddress) throws UnknownHostException {

    EWithInetAddr bean1 = new EWithInetAddr();
    bean1.setName("jim");

    InetAddress address1 = InetAddress.getByName(ipAddress);
    bean1.setInetAddress(address1);

    Ebean.save(bean1);

    EWithInetAddr bean2 = Ebean.find(EWithInetAddr.class, bean1.getId());
    InetAddress address2 = bean2.getInetAddress();
    Assert.assertNotNull(address2.getHostAddress());
    Assert.assertEquals(address1.getHostAddress(), address2.getHostAddress());

    bean2.setName("modJim");
    bean2.setInetAddress(InetAddress.getByName("120.12.20.80"));
    Ebean.save(bean2);
    Ebean.delete(bean2);
  }

  @Test
  public void use_null() throws UnknownHostException {

    EWithInetAddr bean1 = new EWithInetAddr();
    bean1.setName("jim");

    Ebean.save(bean1);

    EWithInetAddr bean2 = Ebean.find(EWithInetAddr.class, bean1.getId());
    InetAddress address2 = bean2.getInetAddress();
    Assert.assertNull(address2);
  }

}

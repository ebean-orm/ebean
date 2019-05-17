package org.tests.basic.type;

import io.ebean.DB;
import io.ebean.TransactionalTestCase;
import io.ebean.types.Cdir;
import io.ebean.types.Inet;
import org.junit.Test;
import org.tests.model.basic.EWithInetAddr;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestInetAddressType extends TransactionalTestCase {

  @Test
  public void testIp4() throws UnknownHostException {

    insertUpdateDeleteFind("120.12.12.56", "120.12.12.56");
  }

  @Test
  public void testIp6() throws UnknownHostException {

    if (isPostgres()) {
      insertUpdateDeleteFind("2001:db8:85a3:0:0:8a2e:370:7334", "2001:db8:85a3::8a2e:370:7334");
    } else {
      insertUpdateDeleteFind("2001:db8:85a3:0:0:8a2e:370:7334", "2001:db8:85a3:0:0:8a2e:370:7334");
    }
  }

  @Test
  public void test_inet_queryIn() {

    List<Inet> addrs = Inet.listOf("120.12.12.56", "120.12.12.57");

    DB.find(EWithInetAddr.class)
      .where()
      .in("inet2", addrs)
      .findList();
  }

  @Test
  public void test_inetAdress_queryIn() throws UnknownHostException {

    List<InetAddress> addrs = new ArrayList<>();
    addrs.add(InetAddress.getByName("120.12.12.56"));
    addrs.add(InetAddress.getByName("120.12.12.57"));

    DB.find(EWithInetAddr.class)
      .where()
      .in("inetAddress", addrs)
      .findList();
  }

  @Test
  public void test_inet_queryEq() {

    DB.find(EWithInetAddr.class)
      .where()
      .eq("inet2", new Inet("120.12.12.58"))
      .findList();
  }

  @Test
  public void test_inet4address_queryEq() throws UnknownHostException {

    DB.find(EWithInetAddr.class)
      .where()
      .eq("inetAddress", InetAddress.getByName("120.12.12.58"))
      .findList();
  }

  @Test
  public void test_inet6address_queryEq() throws UnknownHostException {

    DB.find(EWithInetAddr.class)
      .where()
      .eq("inetAddress", InetAddress.getByName("2001:db8:85a3:0:0:8a2e:370:7334"))
      .findList();
  }

  private void insertUpdateDeleteFind(String ipAddress, String expected) throws UnknownHostException {

    EWithInetAddr bean1 = new EWithInetAddr();
    bean1.setName("jim");

    InetAddress address1 = InetAddress.getByName(ipAddress);
    bean1.setInetAddress(address1);
    bean1.setInet2(new Inet(ipAddress));
    bean1.setCdir(new Cdir(ipAddress));


    DB.save(bean1);

    EWithInetAddr bean2 = DB.find(EWithInetAddr.class, bean1.getId());
    InetAddress address2 = bean2.getInetAddress();
    assertNotNull(address2.getHostAddress());
    assertThat(address1.getHostAddress()).isEqualTo(address2.getHostAddress());
    assertThat(bean2.getInet2().getAddress()).isEqualTo(expected);
    assertThat(bean2.getCdir().getAddress()).isEqualTo(expected);

    bean2.setName("modJim");
    bean2.setInetAddress(InetAddress.getByName("120.12.20.80"));
    bean1.setInet2(new Inet("120.12.20.80"));
    bean1.setCdir(new Cdir("120.12.20.80"));
    DB.save(bean2);
    DB.delete(bean2);
  }

  @Test
  public void testIp6_ranges() {

    insertFindDeleteRange("2001:4f8:3:ba::/64");
    insertFindDeleteRange("2001:4f8:3:ba:2e0:81ff:fe22:d1f1/64");
  }

  private void insertFindDeleteRange(String ipAddressRange)  {

    EWithInetAddr bean1 = new EWithInetAddr();
    bean1.setName("withRange");
    bean1.setInet2(new Inet(ipAddressRange));
    bean1.setCdir(new Cdir(ipAddressRange));

    DB.save(bean1);

    EWithInetAddr bean2 = DB.find(EWithInetAddr.class, bean1.getId());
    assertNotNull(bean2.getInet2());
    assertThat(bean2.getInet2().getAddress()).isEqualTo(ipAddressRange);
    assertThat(bean2.getCdir().getAddress()).isEqualTo(ipAddressRange);

    DB.delete(bean2);
  }

  @Test
  public void use_null() {

    EWithInetAddr bean1 = new EWithInetAddr();
    bean1.setName("jim");

    DB.save(bean1);

    EWithInetAddr bean2 = DB.find(EWithInetAddr.class, bean1.getId());
    assertNull(bean2.getInetAddress());
    assertNull(bean2.getInet2());
  }

}

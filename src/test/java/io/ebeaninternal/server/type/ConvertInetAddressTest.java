package io.ebeaninternal.server.type;

import org.junit.Test;

import java.net.InetAddress;

import static org.junit.Assert.assertEquals;

public class ConvertInetAddressTest {

  @Test
  public void forString() {

    InetAddress addr = ConvertInetAddresses.forString("128.1.10.23");
    assertEquals("128.1.10.23", addr.getHostAddress());

    String ip6addr = "2001:db8:85a3:0:0:8a2e:370:7334";
    InetAddress addr6 = ConvertInetAddresses.forString(ip6addr);
    String uriAddr6 = ConvertInetAddresses.toUriString(addr6);
    assertEquals("[" + ip6addr + "]", uriAddr6);
    assertEquals(ip6addr, addr6.getHostAddress());
  }

  @Test
  public void ipv6_fromHost_getHostAddress() {
    InetAddress addr2 = ConvertInetAddresses.fromHost("2001:4f8:3:ba:2e0:81ff:fe22:d1f1");
    assertEquals("2001:4f8:3:ba:2e0:81ff:fe22:d1f1", addr2.getHostAddress());
  }

  @Test
  public void ipv6_fromHost_getHostAddress_2() {
    InetAddress addr2 = ConvertInetAddresses.fromHost("2001:db8:85a3:0:0:8a2e:370:7334");
    assertEquals("2001:db8:85a3:0:0:8a2e:370:7334", addr2.getHostAddress());
  }

  @Test
  public void toUriString() {

    InetAddress addr = ConvertInetAddresses.forString("128.1.10.23");
    assertEquals("128.1.10.23", addr.getHostAddress());

    String uriAddr = ConvertInetAddresses.toUriString(addr);
    assertEquals("128.1.10.23", uriAddr);
  }

}

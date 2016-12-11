package io.ebeaninternal.server.type;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;

public class ConvertInetAddressTest {

  @Test
  public void forString() {

    InetAddress addr = ConvertInetAddresses.forString("128.1.10.23");
    Assert.assertNotNull(addr);
    Assert.assertEquals("128.1.10.23", addr.getHostAddress());

    String ip6addr = "2001:db8:85a3:0:0:8a2e:370:7334";
    InetAddress addr6 = ConvertInetAddresses.forString(ip6addr);
    String uriAddr6 = ConvertInetAddresses.toUriString(addr6);
    Assert.assertEquals("[" + ip6addr + "]", uriAddr6);
    Assert.assertEquals(ip6addr, addr6.getHostAddress());
  }

  @Test
  public void toUriString() {

    InetAddress addr = ConvertInetAddresses.forString("128.1.10.23");
    Assert.assertEquals("128.1.10.23", addr.getHostAddress());

    String uriAddr = ConvertInetAddresses.toUriString(addr);
    Assert.assertEquals("128.1.10.23", uriAddr);
  }

}

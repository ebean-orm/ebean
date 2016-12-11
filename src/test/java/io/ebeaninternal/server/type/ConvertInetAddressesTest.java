package io.ebeaninternal.server.type;

import org.junit.Test;

import java.net.InetAddress;

import static org.junit.Assert.*;

public class ConvertInetAddressesTest {

  @Test
  public void testForString() throws Exception {

    InetAddress loopbackAddress = InetAddress.getLoopbackAddress();

    String uri = ConvertInetAddresses.toUriString(loopbackAddress);
    InetAddress inetAddress = ConvertInetAddresses.forString(uri);

    assertEquals(loopbackAddress, inetAddress);
  }

  @Test
  public void testIsInetAddress() throws Exception {

    assertTrue(ConvertInetAddresses.isInetAddress("127.0.0.1"));

    assertFalse(ConvertInetAddresses.isInetAddress("junk"));
    assertFalse(ConvertInetAddresses.isInetAddress("127.0.0.junk"));
    assertFalse(ConvertInetAddresses.isInetAddress("junk.0.0.23"));
    assertFalse(ConvertInetAddresses.isInetAddress(""));
    assertFalse(ConvertInetAddresses.isInetAddress("127.0.0"));

  }

  @Test
  public void testToUriString() throws Exception {

    InetAddress loopbackAddress = InetAddress.getLoopbackAddress();

    String uri = ConvertInetAddresses.toUriString(loopbackAddress);
    InetAddress inetAddress = ConvertInetAddresses.forUriString(uri);

    assertEquals(loopbackAddress, inetAddress);
  }

}

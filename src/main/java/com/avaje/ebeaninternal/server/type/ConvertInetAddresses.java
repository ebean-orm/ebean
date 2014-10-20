package com.avaje.ebeaninternal.server.type;

/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * Rob Bygrave: This is a copy of the google guava InetAddresses class 
 * with some features removed.
 * 
 * 
 * Static utility methods pertaining to {@link InetAddress} instances.
 *
 * <p><b>Important note:</b> Unlike {@code InetAddress.getByName()}, the
 * methods of this class never cause DNS services to be accessed. For
 * this reason, you should prefer these methods as much as possible over
 * their JDK equivalents whenever you are expecting to handle only
 * IP address string literals -- there is no blocking DNS penalty for a
 * malformed string.
 *
 * <p>This class hooks into the {@code sun.net.util.IPAddressUtil} class
 * to make use of the {@code textToNumericFormatV4} and
 * {@code textToNumericFormatV6} methods directly as a means to avoid
 * accidentally traversing all nameservices (it can be vitally important
 * to avoid, say, blocking on DNS at times).
 *
 * <p>When dealing with {@link Inet4Address} and {@link Inet6Address}
 * objects as byte arrays (vis. {@code InetAddress.getAddress()}) they
 * are 4 and 16 bytes in length, respectively, and represent the address
 * in network byte order.
 *
 * <p>Examples of IP addresses and their byte representations:
 * <ul>
 * <li>The IPv4 loopback address, {@code "127.0.0.1"}.<br/>
 *     {@code 7f 00 00 01}
 *
 * <li>The IPv6 loopback address, {@code "::1"}.<br/>
 *     {@code 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01}
 *
 * <li>From the IPv6 reserved documentation prefix ({@code 2001:db8::/32}),
 *     {@code "2001:db8::1"}.<br/>
 *     {@code 20 01 0d b8 00 00 00 00 00 00 00 00 00 00 00 01}
 *
 * <li>An IPv6 "IPv4 compatible" (or "compat") address,
 *     {@code "::192.168.0.1"}.<br/>
 *     {@code 00 00 00 00 00 00 00 00 00 00 00 00 c0 a8 00 01}
 *
 * <li>An IPv6 "IPv4 mapped" address, {@code "::ffff:192.168.0.1"}.<br/>
 *     {@code 00 00 00 00 00 00 00 00 00 00 ff ff c0 a8 00 01}
 * </ul>
 *
 * <p>A few notes about IPv6 "IPv4 mapped" addresses and their observed
 * use in Java.
 * <br><br>
 * "IPv4 mapped" addresses were originally a representation of IPv4
 * addresses for use on an IPv6 socket that could receive both IPv4
 * and IPv6 connections (by disabling the {@code IPV6_V6ONLY} socket
 * option on an IPv6 socket).  Yes, it's confusing.  Nevertheless,
 * these "mapped" addresses were never supposed to be seen on the
 * wire.  That assumption was dropped, some say mistakenly, in later
 * RFCs with the apparent aim of making IPv4-to-IPv6 transition simpler.
 *
 * <p>Technically one <i>can</i> create a 128bit IPv6 address with the wire
 * format of a "mapped" address, as shown above, and transmit it in an
 * IPv6 packet header.  However, Java's InetAddress creation methods
 * appear to adhere doggedly to the original intent of the "mapped"
 * address: all "mapped" addresses return {@link Inet4Address} objects.
 *
 * <p>For added safety, it is common for IPv6 network operators to filter
 * all packets where either the source or destination address appears to
 * be a "compat" or "mapped" address.  Filtering suggestions usually
 * recommend discarding any packets with source or destination addresses
 * in the invalid range {@code ::/3}, which includes both of these bizarre
 * address formats.  For more information on "bogons", including lists
 * of IPv6 bogon space, see:
 *
 * <ul>
 * <li><a target="_parent"
 *        href="http://en.wikipedia.org/wiki/Bogon_filtering"
 *       >http://en.wikipedia.org/wiki/Bogon_filtering</a>
 * <li><a target="_parent"
 *        href="http://www.cymru.com/Bogons/ipv6.txt"
 *       >http://www.cymru.com/Bogons/ipv6.txt</a>
 * <li><a target="_parent"
 *        href="http://www.cymru.com/Bogons/v6bogon.html"
 *       >http://www.cymru.com/Bogons/v6bogon.html</a>
 * <li><a target="_parent"
 *        href="http://www.space.net/~gert/RIPE/ipv6-filters.html"
 *       >http://www.space.net/~gert/RIPE/ipv6-filters.html</a>
 * </ul>
 *
 * @author Erik Kline
 * @since 5
 */
//@Beta
public final class ConvertInetAddresses {

  private static final int IPV4_PART_COUNT = 4;
  private static final int IPV6_PART_COUNT = 8;

  private ConvertInetAddresses() {}

  /**
   * Returns the {@link InetAddress} having the given string
   * representation.
   *
   * <p>This deliberately avoids all nameservice lookups (e.g. no DNS).
   *
   * @param ipString {@code String} containing an IPv4 or IPv6 string literal,
   *                 e.g. {@code "192.168.0.1"} or {@code "2001:db8::1"}
   * @return {@link InetAddress} representing the argument
   * @throws IllegalArgumentException if the argument is not a valid
   *         IP string literal
   */
  public static InetAddress forString(String ipString) {
    byte[] addr = textToNumericFormatV4(ipString);
    if (addr == null) {
      // Scanning for IPv4 string literal failed; try IPv6.
      addr = textToNumericFormatV6(ipString);
    }

    // The argument was malformed, i.e. not an IP string literal.
    if (addr == null) {
      throw new IllegalArgumentException(
          String.format("'%s' is not an IP string literal.", ipString));
    }

    try {
      return InetAddress.getByAddress(addr);
    } catch (UnknownHostException e) {

      /*
       * This really shouldn't happen in practice since all our byte
       * sequences should be valid IP addresses.
       *
       * However {@link InetAddress#getByAddress} is documented as
       * potentially throwing this "if IP address is of illegal length".
       *
       * This is mapped to IllegalArgumentException since, presumably,
       * the argument triggered some processing bug in either
       * {@link IPAddressUtil#textToNumericFormatV4} or
       * {@link IPAddressUtil#textToNumericFormatV6}.
       */
      throw new IllegalArgumentException(
          String.format("'%s' is extremely broken.", ipString), e);
    }
  }

  /**
   * Returns {@code true} if the supplied string is a valid IP string
   * literal, {@code false} otherwise.
   *
   * @param ipString {@code String} to evaluated as an IP string literal
   * @return {@code true} if the argument is a valid IP string literal
   */
  public static boolean isInetAddress(String ipString) {
    try {
      forString(ipString);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private static byte[] textToNumericFormatV4(String ipString) {

    boolean isIpv6 = false;

    // handle IPv6 forms of IPv4 addresses
    // TODO: use Ascii.toUpperCase() when available
    if (ipString.toUpperCase(Locale.US).startsWith("::FFFF:")) {
      ipString = ipString.substring(7);
    } else if (ipString.startsWith("::")) {
      ipString = ipString.substring(2);
      isIpv6 = true;
    }

    String[] address = ipString.split("\\.");
    if (address.length != IPV4_PART_COUNT) {
      return null;
    }
    try {
      byte[] bytes = new byte[IPV4_PART_COUNT];
      for (int i = 0; i < bytes.length; i++) {
        int piece = Integer.parseInt(address[i]);
        if (piece < 0 || piece > 255) {
          return null;
        }

        // No leading zeroes are allowed.  See
        // http://tools.ietf.org/html/draft-main-ipaddr-text-rep-00
        // section 2.1 for discussion.

        if (address[i].startsWith("0") && address[i].length() != 1) {
          return null;
        }
        bytes[i] = (byte) piece;
      }

      if (isIpv6) { // prepend with zeroes;
        byte[] data = new byte[2 * IPV6_PART_COUNT]; // Java initializes arrays to zero
        System.arraycopy(bytes, 0, data, 12, IPV4_PART_COUNT);
        return data;
      } else {
        return bytes;
      }
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private static byte[] textToNumericFormatV6(String ipString) {
    if (!ipString.contains(":")) {
      return null;
    }
    if (ipString.contains(":::")) {
      return null;
    }

    if (ipString.contains(".")) {
      ipString = convertDottedQuadToHex(ipString);
      if (ipString == null) {
        return null;
      }
    }

    ipString = padIpString(ipString);
    try {
      String[] address = ipString.split(":", IPV6_PART_COUNT);
      if (address.length != IPV6_PART_COUNT) {
        return null;
      }
      byte[] bytes = new byte[2 * IPV6_PART_COUNT];
      for (int i = 0; i < IPV6_PART_COUNT; i++) {
        int piece = address[i].equals("") ? 0 : Integer.parseInt(address[i], 16);
        bytes[2 * i] = (byte) ((piece & 0xFF00) >>> 8);
        bytes[2 * i + 1] = (byte) (piece & 0xFF);
      }
      return bytes;
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  // Fill in any omitted colons
  private static String padIpString(String ipString) {
    if (ipString.contains("::")) {
      int count = numberOfColons(ipString);
      StringBuilder buffer = new StringBuilder("::");
      for (int i = 0; i + count < 7; i++) {
        buffer.append(":");
      }
      ipString = ipString.replace("::", buffer);
    }
    return ipString;
  }

  private static int numberOfColons(String s) {
    int count = 0;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == ':') {
        count++;
      }
    }
    return count;
  }

  private static String convertDottedQuadToHex(String ipString) {
    int lastColon = ipString.lastIndexOf(':');
    String initialPart = ipString.substring(0, lastColon + 1);
    String dottedQuad = ipString.substring(lastColon + 1);
    byte[] quad = textToNumericFormatV4(dottedQuad);
    if (quad == null) {
      return null;
    }
    String penultimate = Integer.toHexString(((quad[0] & 0xff) << 8) | (quad[1] & 0xff));
    String ultimate = Integer.toHexString(((quad[2] & 0xff) << 8) | (quad[3] & 0xff));
    return initialPart + penultimate + ":" + ultimate;
  }

  /**
   * Returns the string representation of an {@link InetAddress} suitable
   * for inclusion in a URI.
   *
   * <p>For IPv4 addresses, this is identical to
   * {@link InetAddress#getHostAddress()}, but for IPv6 addresses it
   * surrounds this text with square brackets; for example
   * {@code "[2001:db8::1]"}.
   *
   * <p>Per section 3.2.2 of
   * <a target="_parent"
   *    href="http://tools.ietf.org/html/rfc3986#section-3.2.2"
   *  >http://tools.ietf.org/html/rfc3986</a>,
   * a URI containing an IPv6 string literal is of the form
   * {@code "http://[2001:db8::1]:8888/index.html"}.
   *
   * <p>Use of either {@link InetAddress#getHostAddress()} or this
   * method is recommended over {@link InetAddress#toString()} when an
   * IP address string literal is desired.  This is because
   * {@link InetAddress#toString()} prints the hostname and the IP
   * address string joined by a "/".
   *
   * @param ip {@link InetAddress} to be converted to URI string literal
   * @return {@code String} containing URI-safe string literal
   */
  public static String toUriString(InetAddress ip) {
    if (ip instanceof Inet6Address) {
      return "[" + ip.getHostAddress() + "]";
    }
    return ip.getHostAddress();
  }

  /**
   * Returns an InetAddress representing the literal IPv4 or IPv6 host
   * portion of a URL, encoded in the format specified by RFC 3986 section 3.2.2.
   *
   * <p>This function is similar to {@link ConvertInetAddresses#forString(String)},
   * however, it requires that IPv6 addresses are surrounded by square brackets.
   *
   * <p>This function is the inverse of
   * {@link ConvertInetAddresses#toUriString(java.net.InetAddress)}.
   *
   * @param hostAddr A RFC 3986 section 3.2.2 encoded IPv4 or IPv6 address
   * @return an InetAddress representing the address in {@code hostAddr}
   * @throws IllegalArgumentException if {@code hostAddr} is not a valid
   *     IPv4 address, or IPv6 address surrounded by square brackets
   */
  public static InetAddress forUriString(String hostAddr) {
    //Preconditions.checkNotNull(hostAddr);
    //Preconditions.checkArgument(hostAddr.length() > 0, "host string is empty");
    InetAddress retval = null;

    // IPv4 address?
    try {
      retval = forString(hostAddr);
      if (retval instanceof Inet4Address) {
        return retval;
      }
    } catch (IllegalArgumentException e) {
      // Not a valid IP address, fall through.
    }

    // IPv6 address
    if (!(hostAddr.startsWith("[") && hostAddr.endsWith("]"))) {
      throw new IllegalArgumentException("Not a valid address: \"" + hostAddr + '"');
    }

    retval = forString(hostAddr.substring(1, hostAddr.length() - 1));
    if (retval instanceof Inet6Address) {
      return retval;
    }

    throw new IllegalArgumentException("Not a valid address: \"" + hostAddr + '"');
  }

}

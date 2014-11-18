package com.avaje.ebeaninternal.server.type;

/*
 * Copyright 2013 FasterXML.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

/**
 * Utilities to aid in the translation of decimal types to/from multiple parts.
 *
 * @author Nick Williams
 * @since 2.2.0
 */
public final class DecimalUtils {

  private static final char[] ZEROES = new char[]{'0', '0', '0', '0', '0', '0', '0', '0', '0'};

  private static final BigDecimal ONE_BILLION = new BigDecimal(1000000000L);

  private DecimalUtils() {
    throw new RuntimeException("DecimalUtils cannot be instantiated.");
  }

  public static Duration toDuration(BigDecimal value) {

    long seconds = value.longValue();
    int nanoseconds = extractNanosecondDecimal(value, seconds);

    return Duration.ofSeconds(seconds, nanoseconds);
  }

  public static BigDecimal toDecimal(Duration instant) {
    return new BigDecimal(toDecimal(instant.getSeconds(), instant.getNano()));
  }

  public static Timestamp toTimestamp(BigDecimal value) {

    long seconds = value.longValue();
    int nanoseconds = extractNanosecondDecimal(value, seconds);

    Timestamp ts = new Timestamp(seconds * 1000);
    ts.setNanos(nanoseconds);
    return ts;
  }

  public static BigDecimal toDecimal(Timestamp instant) {
    long millis = instant.getTime();
    long secs = millis/1000;
    return new BigDecimal(toDecimal(secs, instant.getNanos()));
  }

  public static Instant toInstant(BigDecimal value) {

    long seconds = value.longValue();
    int nanoseconds = extractNanosecondDecimal(value, seconds);
    return Instant.ofEpochSecond(seconds, nanoseconds);
  }

  public static BigDecimal toDecimal(Instant instant) {
    return new BigDecimal(toDecimal(instant.getEpochSecond(), instant.getNano()));
  }

  public static String toDecimal(long seconds, int nanoseconds) {
    StringBuilder string = new StringBuilder(Integer.toString(nanoseconds));
    if (string.length() < 9)
      string.insert(0, ZEROES, 0, 9 - string.length());
    return seconds + "." + string;
  }

  public static int extractNanosecondDecimal(BigDecimal value, long integer) {
    return value.subtract(new BigDecimal(integer)).multiply(ONE_BILLION).intValue();
  }
}
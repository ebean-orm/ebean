/*
 * Licensed Materials - Property of FOCONIS AG
 * (C) Copyright FOCONIS AG.
 */

package io.ebeaninternal.server.querydefn;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import io.ebeaninternal.api.BindHash;

/**
 * Bind hash that uses a MessageDigest to compute a collision resistent hash.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class MdBindHash implements BindHash {
  private MessageDigest md;
  private byte[] buffer;
  private int hashCode;

  public MdBindHash(String algorithm) {
    try {
      md = MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException nsae) {
      throw new InternalError(algorithm + " not supported", nsae);
    }
  }

  @Override
  public BindHash update(int v) {
    md.update((byte) (v >>> 24));
    md.update((byte) (v >>> 16));
    md.update((byte) (v >>> 8));
    md.update((byte) (v >>> 0));
    return this;
  }

  @Override
  public BindHash update(long v) {
    md.update((byte) (v >>> 56));
    md.update((byte) (v >>> 48));
    md.update((byte) (v >>> 40));
    md.update((byte) (v >>> 32));
    md.update((byte) (v >>> 24));
    md.update((byte) (v >>> 16));
    md.update((byte) (v >>> 8));
    md.update((byte) (v >>> 0));
    return this;
  }

  @Override
  public BindHash update(boolean boolValue) {
    md.update(boolValue ? (byte) 1 : (byte) 0);
    return this;
  }

  @Override
  public BindHash update(Object value) {
    if (value == null) {
      md.update((byte) 0);

      // do some special handling for known object types
    } else if (value instanceof String) {
      md.update(((String) value).getBytes());

    } else if (value instanceof Long) {
      update(((Long) value).longValue());

    } else if (value instanceof Double) {
      double d = ((Double) value).doubleValue();
      update(Double.doubleToLongBits(d));

    } else if (value instanceof UUID) {
      UUID uuid = (UUID) value;
      update(uuid.getLeastSignificantBits());
      update(uuid.getMostSignificantBits());

    } else if (value instanceof Date) {
      update(((Date) value).getTime());

    } else if (value instanceof Instant) {
      update(((Instant) value).getEpochSecond());
      update(((Instant) value).getNano());

    } else if (value instanceof LocalDate) {
      update(((LocalDate) value).toEpochDay());

    } else if (value instanceof LocalTime) {
      update(((LocalTime) value).toSecondOfDay());
      update(((LocalTime) value).toNanoOfDay());

    } else if (value instanceof LocalDateTime) {
      update(((LocalDateTime) value).toLocalDate().toEpochDay());
      update(((LocalDateTime) value).toLocalTime().toSecondOfDay());
      update(((LocalDateTime) value).toLocalTime().toNanoOfDay());

    } else {
      // Fall back to hashCode for all other types
      updateOther(value);
    }
    return this;
  }

  /**
   * Update all other object. May be overridden to handle joda dates.
   */
  protected void updateOther(Object value) {
    // Fall back to hashCode for all other types
    update(value.hashCode());
  }

  @Override
  public void finish() {
    buffer = md.digest();
    hashCode = Arrays.hashCode(buffer);
    md = null; // clear memory
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof MdBindHash && Arrays.equals(buffer, ((MdBindHash) obj).buffer);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }
}

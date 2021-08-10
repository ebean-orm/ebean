package io.ebeaninternal.server.querydefn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.ebeaninternal.api.BindHash;

/**
 * HashCode builder that uses Object.hashCode for computing bind-hashes.
 * This is a fast and lightweight implementation, but may produce collisions.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class HashCodeBindHash implements BindHash {

  private final List<Object> values = new ArrayList<>();
  private int hashCode;

  @Override
  public BindHash update(int intValue) {
    values.add(intValue);
    hashCode = hashCode * 92821 + intValue;
    return this;
  }

  @Override
  public BindHash update(long longValue) {
    values.add(longValue);
    hashCode = hashCode * 92821 + Long.hashCode(longValue);
    return this;
  }

  @Override
  public BindHash update(boolean boolValue) {
    values.add(boolValue);
    hashCode = hashCode * 92821 + Boolean.hashCode(boolValue);
    return this;
  }

  @Override
  public BindHash update(Object value) {
    values.add(value);
    hashCode = hashCode * 92821 + Objects.hashCode(value);
    return this;
  }

  @Override
  public void finish() {
    // nothing to do
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof HashCodeBindHash && ((HashCodeBindHash) obj).values.equals(values);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

}

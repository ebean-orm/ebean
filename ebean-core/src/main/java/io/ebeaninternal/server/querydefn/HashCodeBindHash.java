package io.ebeaninternal.server.querydefn;

import java.util.Objects;

import io.ebeaninternal.api.BindHash;

/**
 * HashCode builder that uses Object.hashCode for computing bind-hashes.
 * This is a fast and lightweight implementation, but may produce collisions.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class HashCodeBindHash implements BindHash {

  int hashCode;

  @Override
  public BindHash update(int intValue) {
    hashCode = hashCode * 92821 + intValue;
    return this;
  }

  @Override
  public BindHash update(long longValue) {
    hashCode = hashCode * 92821 + Long.hashCode(longValue);
    return this;
  }

  @Override
  public BindHash update(boolean boolValue) {
    hashCode = hashCode * 92821 + Boolean.hashCode(boolValue);
    return this;
  }

  @Override
  public BindHash update(Object value) {
    hashCode = hashCode * 92821 + Objects.hashCode(value);
    return this;
  }

  @Override
  public void finish() {
    // nothing to do
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof HashCodeBindHash && ((HashCodeBindHash) obj).hashCode == hashCode;
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

}

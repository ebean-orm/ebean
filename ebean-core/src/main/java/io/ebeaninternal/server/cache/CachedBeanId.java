package io.ebeaninternal.server.cache;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Bean Id value plus discriminator type.
 * <p>
 * Put into L2 cache such that we know the type of a bean with inheritance.
 */
public class CachedBeanId implements Externalizable {

  private String discValue;
  private Object id;

  public CachedBeanId(String discValue, Object id) {
    this.discValue = discValue;
    this.id = id;
  }

  /**
   * Construct from serialisation.
   */
  public CachedBeanId() {
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(discValue);
    out.writeObject(id);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    discValue = in.readUTF();
    id = in.readObject();
  }

  @Override
  public String toString() {
    return discValue + ":" + id;
  }

  public String getDiscValue() {
    return discValue;
  }

  public Object getId() {
    return id;
  }

}

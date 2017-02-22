package io.ebeaninternal.server.cache;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * The cached data for O2M and M2M relationships.
 * <p>
 * This is effectively just the Id values for each of the beans in the collection.
 * </p>
 */
public class CachedManyIds implements Externalizable {

  private List<Object> idList;

  public CachedManyIds(List<Object> idList) {
    this.idList = idList;
  }

  /**
   * Construct for serialization.
   */
  public CachedManyIds() {
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(idList.size());
    for (Object id : idList) {
      out.writeObject(id);
    }
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    int size = in.readInt();
    idList = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      idList.add(in.readObject());
    }
  }

  @Override
  public String toString() {
    return idList.toString();
  }

  public List<Object> getIdList() {
    return idList;
  }

}

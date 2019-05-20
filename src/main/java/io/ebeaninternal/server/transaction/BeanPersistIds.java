package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.BinaryReadContext;
import io.ebeaninternal.api.BinaryWritable;
import io.ebeaninternal.api.BinaryWriteContext;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.cache.CacheChangeSet;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.id.IdBinder;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Wraps the information representing a Inserted Updated or Deleted Bean.
 * <p>
 * This information is broadcast across the cluster so that remote BeanListeners
 * are notified of the inserts updates and deletes that occurred.
 * </p>
 * <p>
 * You control it the data is broadcast and what data is broadcast by the
 * BeanListener.getClusterData() method. It is guessed that often just the Id
 * property or perhaps a few properties in a Map will be broadcast to reduce the
 * size of data sent around the network.
 * </p>
 */
public class BeanPersistIds implements BinaryWritable {

  private final BeanDescriptor<?> beanDescriptor;

  private final String descriptorId;

  /**
   * The ids to invalidate from the cache (updates and deletes).
   */
  private List<Object> ids;

  /**
   * Create the payload.
   */
  public BeanPersistIds(BeanDescriptor<?> desc) {
    this.beanDescriptor = desc;
    this.descriptorId = desc.getDescriptorId();
  }

  public static BeanPersistIds readBinaryMessage(SpiEbeanServer server, BinaryReadContext input) throws IOException {

    BeanDescriptor<?> desc = server.getBeanDescriptorById(input.readUTF());
    BeanPersistIds bp = new BeanPersistIds(desc);
    bp.read(input);
    return bp;
  }

  private void read(BinaryReadContext dataInput) throws IOException {

    dataInput.readInt(); // legacy read type
    ids = readIdList(dataInput.in(), beanDescriptor.getIdBinder());
  }

  @Override
  public void writeBinary(BinaryWriteContext out) throws IOException {

    DataOutputStream os = out.start(TYPE_BEANIUD);
    os.writeUTF(descriptorId);
    os.writeInt(1); // legacy marker for update
    if (ids == null) {
      os.writeInt(0);
    } else {
      os.writeInt(ids.size());
      IdBinder idBinder = beanDescriptor.getIdBinder();
      for (Object idValue : ids) {
        idBinder.writeData(os, idValue);
      }
    }
  }

  private List<Object> readIdList(DataInput dataInput, IdBinder idBinder) throws IOException {

    int count = dataInput.readInt();
    if (count < 1) {
      return null;
    }
    List<Object> idList = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      idList.add(idBinder.readData(dataInput));
    }
    return idList;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("BeanIds[");
    if (beanDescriptor != null) {
      sb.append(beanDescriptor.getFullName());
    } else {
      sb.append("descId:").append(descriptorId);
    }
    if (ids != null) {
      sb.append(" ids:").append(ids);
    }
    sb.append("]");
    return sb.toString();
  }

  public void addId(PersistRequest.Type type, Object id) {
    if (type != PersistRequest.Type.INSERT) {
      if (ids == null) {
        ids = new ArrayList<>();
      }
      ids.add(id);
    }
  }

  public BeanDescriptor<?> getBeanDescriptor() {
    return beanDescriptor;
  }

  public List<Object> getIds() {
    return ids;
  }

  /**
   * Notify the cache of this event that came from another server in the cluster.
   */
  public void notifyCache(CacheChangeSet changeSet) {
    changeSet.addClearQuery(beanDescriptor);
    if (ids != null) {
      changeSet.addBeanRemoveMany(beanDescriptor, ids);
    }
  }
}

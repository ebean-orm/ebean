package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.BinaryReadContext;
import io.ebeaninternal.api.BinaryWritable;
import io.ebeaninternal.api.BinaryWriteContext;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.id.IdBinder;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
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

  private List<Object> insertIds;
  private List<Object> updateIds;
  private List<Object> deleteIds;

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

    IdBinder idBinder = beanDescriptor.getIdBinder();

    int iudType = dataInput.readInt();
    List<Object> idList = readIdList(dataInput.in(), idBinder);
    switch (iudType) {
      case 0:
        insertIds = idList;
        break;
      case 1:
        updateIds = idList;
        break;
      case 2:
        deleteIds = idList;
        break;

      default:
        throw new RuntimeException("Invalid iudType " + iudType);
    }
  }

  @Override
  public void writeBinary(BinaryWriteContext out) throws IOException {
    writeBinaryList(out, 0, insertIds);
    writeBinaryList(out, 1, updateIds);
    writeBinaryList(out, 2, deleteIds);
  }

  private void writeBinaryList(BinaryWriteContext out, int type, List<Object> ids) throws IOException {

    if (ids != null) {
      IdBinder idBinder = beanDescriptor.getIdBinder();
      int count = ids.size();

      DataOutputStream os = out.start(TYPE_BEANIUD);
      os.writeUTF(descriptorId);
      os.writeInt(type);
      os.writeInt(count);
      for (Object insertId : ids) {
        idBinder.writeData(os, insertId);
      }
      os.flush();
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
    if (beanDescriptor != null) {
      sb.append(beanDescriptor.getFullName());
    } else {
      sb.append("descId:").append(descriptorId);
    }
    if (insertIds != null) {
      sb.append(" insertIds:").append(insertIds);
    }
    if (updateIds != null) {
      sb.append(" updateIds:").append(updateIds);
    }
    if (deleteIds != null) {
      sb.append(" deleteIds:").append(deleteIds);
    }
    return sb.toString();
  }

  public void addId(PersistRequest.Type type, Serializable id) {
    switch (type) {
      case INSERT:
        addInsertId(id);
        break;
      case UPDATE:
        addUpdateId(id);
        break;
      case DELETE:
      case DELETE_SOFT:
        addDeleteId(id);
        break;

      default:
        break;
    }
  }

  private void addInsertId(Serializable id) {
    if (insertIds == null) {
      insertIds = new ArrayList<>();
    }
    insertIds.add(id);
  }

  private void addUpdateId(Serializable id) {
    if (updateIds == null) {
      updateIds = new ArrayList<>();
    }
    updateIds.add(id);
  }

  private void addDeleteId(Serializable id) {
    if (deleteIds == null) {
      deleteIds = new ArrayList<>();
    }
    deleteIds.add(id);
  }

  public BeanDescriptor<?> getBeanDescriptor() {
    return beanDescriptor;
  }

  public List<Object> getDeleteIds() {
    return deleteIds;
  }

  public List<Object> getInsertIds() {
    return insertIds;
  }

  public List<Object> getUpdateIds() {
    return updateIds;
  }

  /**
   * Notify the cache of this event that came from another server in the cluster.
   */
  void notifyCacheAndListener() {

    // any change invalidates the query cache
    beanDescriptor.clearQueryCache();

    if (updateIds != null) {
      for (Object id : updateIds) {
        beanDescriptor.cacheHandleDeleteById(id);
      }
    }
    if (deleteIds != null) {
      for (Object id : deleteIds) {
        beanDescriptor.cacheHandleDeleteById(id);
      }
    }
  }
}

package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.cluster.BinaryMessage;
import io.ebeaninternal.server.cluster.BinaryMessageList;
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
public class BeanPersistIds {

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

  public static BeanPersistIds readBinaryMessage(SpiEbeanServer server, DataInput dataInput) throws IOException {

    String descriptorId = dataInput.readUTF();
    BeanDescriptor<?> desc = server.getBeanDescriptorById(descriptorId);
    BeanPersistIds bp = new BeanPersistIds(desc);
    bp.read(dataInput);
    return bp;
  }

  private void read(DataInput dataInput) throws IOException {

    IdBinder idBinder = beanDescriptor.getIdBinder();

    int iudType = dataInput.readInt();
    List<Object> idList = readIdList(dataInput, idBinder);
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

  /**
   * Write the contents into a BinaryMessage form.
   * <p>
   * For a RemoteBeanPersist with a large number of id's note that this is
   * broken up into many BinaryMessages each with a maximum of 100 ids. This
   * enables the contents of a large RemoteTransactionEvent to be split up
   * across multiple Packets.
   * </p>
   */
  void writeBinaryMessage(BinaryMessageList msgList) throws IOException {

    writeIdList(beanDescriptor, 0, insertIds, msgList);
    writeIdList(beanDescriptor, 1, updateIds, msgList);
    writeIdList(beanDescriptor, 2, deleteIds, msgList);
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

  /**
   * Write a BinaryMessage containing the descriptorId, iudType and list of Id
   * values.
   * <p>
   * Note that a given BinaryMessage has a maximum of 100 Ids. This is due to
   * the limit of UDP packet sizes. We break up the RemoteBeanPersist into
   * potentially many smaller BinaryMessages which may be put into multiple
   * Packets.
   * </p>
   */
  private void writeIdList(BeanDescriptor<?> desc, int iudType, List<Object> idList, BinaryMessageList msgList) throws IOException {

    IdBinder idBinder = desc.getIdBinder();

    int count = idList == null ? 0 : idList.size();
    if (count > 0) {
      int loop = 0;
      int i = 0;
      int eof = idList.size();
      do {
        ++loop;
        int endOfLoop = Math.min(eof, loop * 100);

        BinaryMessage m = new BinaryMessage(endOfLoop * 4 + 20);

        DataOutputStream os = m.getOs();
        os.writeInt(BinaryMessage.TYPE_BEANIUD);
        os.writeUTF(descriptorId);
        os.writeInt(iudType);
        os.writeInt(count);

        for (; i < endOfLoop; i++) {
          idBinder.writeData(os, idList.get(i));
        }

        os.flush();
        msgList.add(m);

      } while (i < eof);
    }
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

  void addId(PersistRequest.Type type, Serializable id) {
    switch (type) {
      case INSERT:
        addInsertId(id);
        break;
      case UPDATE:
        addUpdateId(id);
        break;
      case DELETE:
      case SOFT_DELETE:
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

  List<Object> getDeleteIds() {
    return deleteIds;
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

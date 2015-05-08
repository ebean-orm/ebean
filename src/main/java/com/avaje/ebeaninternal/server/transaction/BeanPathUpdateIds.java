package com.avaje.ebeaninternal.server.transaction;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.cluster.BinaryMessage;
import com.avaje.ebeaninternal.server.cluster.BinaryMessageList;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.id.IdBinder;

public class BeanPathUpdateIds {
    
    private transient BeanDescriptor<?> beanDescriptor;

    private final String descriptorId;

    private String path;
    
    private ArrayList<Serializable> ids;
    
    /**
     * Create the payload.
     */
    public BeanPathUpdateIds(BeanDescriptor<?> desc, String path) {
        this.beanDescriptor = desc;
        this.descriptorId = desc.getDescriptorId();
        this.path = path;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (beanDescriptor != null) {
            sb.append(beanDescriptor.getFullName());
        } else {
            sb.append("descId:").append(descriptorId);
        }
        sb.append(" path:").append(path);
        sb.append(" ids:").append(ids);
        return sb.toString();
    }

    public static BeanPathUpdateIds readBinaryMessage(SpiEbeanServer server, DataInput dataInput) throws IOException {

        String descriptorId = dataInput.readUTF();
        String path = dataInput.readUTF();
        BeanDescriptor<?> desc = server.getBeanDescriptorById(descriptorId);
        BeanPathUpdateIds bp = new BeanPathUpdateIds(desc, path);
        bp.read(dataInput);
        return bp;
    }

    private void read(DataInput dataInput) throws IOException {

        IdBinder idBinder = beanDescriptor.getIdBinder();
        ids = readIdList(dataInput, idBinder);        
    }


    private ArrayList<Serializable> readIdList(DataInput dataInput, IdBinder idBinder) throws IOException {

        int count = dataInput.readInt();
        if (count < 1) {
            return null;
        }
        ArrayList<Serializable> idList = new ArrayList<Serializable>(count);
        for (int i = 0; i < count; i++) {
            Object id = idBinder.readData(dataInput);
            idList.add((Serializable) id);
        }
        return idList;
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
    public void writeBinaryMessage(BinaryMessageList msgList) throws IOException {

        IdBinder idBinder = beanDescriptor.getIdBinder();

        int count = ids == null ? 0 : ids.size();
        if (count > 0) {
            int loop = 0;
            int i = 0;
            int eof = ids.size();
            do {
                ++loop;
                int endOfLoop = Math.min(eof, loop * 100);

                BinaryMessage m = new BinaryMessage(endOfLoop * 4 + 20);
                
                DataOutputStream os = m.getOs();
                os.writeInt(BinaryMessage.TYPE_BEANPATHUPDATE);
                os.writeUTF(descriptorId);
                os.writeUTF(path);
                os.writeInt(count);

                for (; i < endOfLoop; i++) {
                    Serializable idValue = ids.get(i);
                    idBinder.writeData(os, idValue);
                }

                os.flush();
                msgList.add(m);

            } while (i < eof);
        }
    }
    
    public void addId(Serializable id) {
        ids.add(id);
    }
    

    public BeanDescriptor<?> getBeanDescriptor() {
        return beanDescriptor;
    }

    /**
     * Return the Descriptor Id. A more compact alternative to using the
     * beanType.
     */
    public String getDescriptorId() {
        return descriptorId;
    }

    public String getPath() {
        return path;
    }

    public List<Serializable> getIds() {
        return ids;
    }
}

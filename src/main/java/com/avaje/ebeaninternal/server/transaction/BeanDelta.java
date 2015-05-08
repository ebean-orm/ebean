package com.avaje.ebeaninternal.server.transaction;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.cluster.BinaryMessage;
import com.avaje.ebeaninternal.server.cluster.BinaryMessageList;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

public class BeanDelta {

    private final List<BeanDeltaProperty> properties;
    
    private final BeanDescriptor<?> beanDescriptor;
    
    private final Object id;
    
    public BeanDelta(BeanDescriptor<?> beanDescriptor, Object id) {
        this.beanDescriptor = beanDescriptor;
        this.id = id;
        this.properties = new ArrayList<BeanDeltaProperty>();
    }
    
    public BeanDescriptor<?> getBeanDescriptor() {
        return beanDescriptor;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BeanDelta[");
        sb.append(beanDescriptor.getName()).append(":");
        sb.append(properties);
        sb.append("]");
        return sb.toString();
    }

    public Object getId() {
        return id;
    }

    public void add(BeanProperty beanProperty, Object value) {
        this.properties.add(new BeanDeltaProperty(beanProperty, value));
    }
    
    public void add(BeanDeltaProperty propertyDelta) {
        this.properties.add(propertyDelta);
    }
    
    public void apply(EntityBean bean) {
        
        for (int i = 0; i < properties.size(); i++) {
            properties.get(i).apply(bean);
        }
    }
    
    /**
     * Read and return a BeanDelta from the binary input.
     */
    public static BeanDelta readBinaryMessage(SpiEbeanServer server, DataInput dataInput) throws IOException {

        String descriptorId = dataInput.readUTF();
        BeanDescriptor<?> desc = server.getBeanDescriptorById(descriptorId);
        Object id = desc.getIdBinder().readData(dataInput);
        BeanDelta bp = new BeanDelta(desc, id);
        
        int count = dataInput.readInt();
        for (int i = 0; i < count; i++) {
            String propName = dataInput.readUTF();
            BeanProperty beanProperty = desc.getBeanProperty(propName);
            Object value = beanProperty.getScalarType().readData(dataInput);
            bp.add(beanProperty, value);
        }
        return bp;
    }
    
    /**
     * Write this bean delta in binary message format.
     */
    public void writeBinaryMessage(BinaryMessageList msgList) throws IOException {

        BinaryMessage m = new BinaryMessage(50);
        
        DataOutputStream os = m.getOs();
        os.writeInt(BinaryMessage.TYPE_BEANDELTA);
        os.writeUTF(beanDescriptor.getDescriptorId());
        
        beanDescriptor.getIdBinder().writeData(os, id);
        os.writeInt(properties.size());
        
        for (int i = 0; i < properties.size(); i++) {
            properties.get(i).writeBinaryMessage(m);
        }

        os.flush();
        msgList.add(m);
    }
}

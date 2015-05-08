package com.avaje.ebeaninternal.server.transaction;

import java.io.DataOutputStream;
import java.io.IOException;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.cluster.BinaryMessage;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

public class BeanDeltaProperty {

    private final BeanProperty beanProperty;
    
    private final Object value;
    
    public BeanDeltaProperty(BeanProperty beanProperty, Object value) {
        this.beanProperty = beanProperty;
        this.value = value;
    }
    
    public String toString() {
        return beanProperty.getName()+":"+value;
    }
    
    public void apply(EntityBean bean) {
        beanProperty.setValue(bean, value);
    }
    
    public void writeBinaryMessage(BinaryMessage m) throws IOException {

        DataOutputStream os = m.getOs();
        os.writeUTF(beanProperty.getName());
        beanProperty.getScalarType().writeData(os, value);
    }
    
}

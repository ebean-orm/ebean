package com.avaje.ebeaninternal.server.transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.server.cluster.BinaryMessageList;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

public class BeanDeltaList {

    private final BeanDescriptor<?> beanDescriptor;
    
    private final List<BeanDelta> deltaBeans = new ArrayList<BeanDelta>();

    public BeanDeltaList(BeanDescriptor<?> beanDescriptor) {
        this.beanDescriptor = beanDescriptor;
    }

    public String toString() {
        return deltaBeans.toString();
    }
    
    public BeanDescriptor<?> getBeanDescriptor() {
        return beanDescriptor;
    }

    public void add(BeanDelta b) {
        deltaBeans.add(b);
    }
    
    public List<BeanDelta> getDeltaBeans() {
        return deltaBeans;
    }

    public void writeBinaryMessage(BinaryMessageList msgList) throws IOException {
        for (int i = 0; i < deltaBeans.size(); i++) {
            deltaBeans.get(i).writeBinaryMessage(msgList);
        }
    }
    
}

package com.avaje.ebeaninternal.server.cluster;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a List of BinaryMessage's.
 * 
 * @author rbygrave
 */
public class BinaryMessageList {

    ArrayList<BinaryMessage> list = new ArrayList<BinaryMessage>();
    
    public void add(BinaryMessage msg) {
        list.add(msg);
    }

    public List<BinaryMessage> getList() {
        return list;
    }
    
}

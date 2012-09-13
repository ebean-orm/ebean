package com.avaje.tests.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.avaje.tests.model.basic.Order;

@XmlRootElement
public class XHeader {

    String requestedBy;
    
    String requestedSystem;
    
    List<Order> orders;

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getRequestedSystem() {
        return requestedSystem;
    }

    public void setRequestedSystem(String requestedSystem) {
        this.requestedSystem = requestedSystem;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
    
}

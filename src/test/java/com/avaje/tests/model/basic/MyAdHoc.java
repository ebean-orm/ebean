package com.avaje.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.avaje.ebean.annotation.Sql;

@Entity
@Sql
public class MyAdHoc {

    @ManyToOne
    Order order;
    
    int detailCount;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public int getDetailCount() {
        return detailCount;
    }

    public void setDetailCount(int detailCount) {
        this.detailCount = detailCount;
    }
    
}

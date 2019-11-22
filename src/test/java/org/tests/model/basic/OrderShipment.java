package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.Timestamp;

@Entity
@Table(name = "or_order_ship")
public class OrderShipment extends BasicDomain {

  public static final int JSON_VERSION = 17; // to test Json-Migration

  private static final long serialVersionUID = 1L;

  @ManyToOne
  private Order order;

  private Timestamp shipTime = new Timestamp(System.currentTimeMillis());

  public Timestamp getShipTime() {
    return shipTime;
  }

  public void setShipTime(Timestamp shipTime) {
    this.shipTime = shipTime;
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }


  // in version 15->16 we have changed the field name "updatedtime" -> "cretime"
  // and changed the resolution from seconds to millis
  public static int migrateJson15(ObjectNode node, ObjectMapper mapper) {
    node.put("updtime", node.remove("updatedtime").longValue() * 1000);
    node.put("createdtime", node.get("createdtime").longValue() * 1000);
    return 16;
  }

  // in version 16->17 we have changed the field name "createdtime" -> "cretime"
  public static int migrateJson16(ObjectNode node, ObjectMapper mapper) {
    node.set("cretime", node.remove("createdtime"));
    return 17;
  }

}

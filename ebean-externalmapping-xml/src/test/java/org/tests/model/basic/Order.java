package org.tests.model.basic;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "o_order")
public class Order extends BasicDomain {

  public enum Status {
    NEW,
    APPROVED,
    SHIPPED,
    COMPLETE
  }

  @Enumerated(value = EnumType.ORDINAL)
  Status status = Status.NEW;
  LocalDate orderDate;
  LocalDate shipDate;

  @ManyToOne(cascade = CascadeType.PERSIST)
  Customer customer;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
  @OrderBy("id asc, orderQty asc, cretime desc")
  List<OrderDetail> details;

}

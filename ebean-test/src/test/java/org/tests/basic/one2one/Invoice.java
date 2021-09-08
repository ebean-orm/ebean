package org.tests.basic.one2one;

import javax.persistence.*;

@Entity
@Table(name = "drel_invoice")
public class Invoice {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;

  @Version
  private int version;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "booking")//_xid", referencedColumnName = "booking_uid")
  private Booking booking;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public Booking getBooking() {
    return booking;
  }

  public void setBooking(Booking booking) {
    this.booking = booking;
  }
}

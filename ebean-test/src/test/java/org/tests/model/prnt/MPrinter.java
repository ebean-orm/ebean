package org.tests.model.prnt;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class MPrinter {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @Column(name = "flags", nullable = false)
  long allFlags = 0L;

  @Column(name = "dwid", nullable = false)
  MSomeOther dataWarehouseId;

  @JoinColumn(name = "current_state_id")
  @ManyToOne
  MPrinterState currentState;

  @JoinColumn(name = "last_swap_cyan_id")
  @OneToOne
  private MPrinterState lastTonerSwapCyan;
  @JoinColumn(name = "last_swap_magenta_id")
  @OneToOne
  private MPrinterState lastTonerSwapMagenta;
  @JoinColumn(name = "last_swap_yellow_id")
  @OneToOne
  private MPrinterState lastTonerSwapYellow;
  @JoinColumn(name = "last_swap_black_id")
  @OneToOne
  private MPrinterState lastTonerSwapBlack;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getAllFlags() {
    return allFlags;
  }

  public void setAllFlags(long allFlags) {
    this.allFlags = allFlags;
  }

  public MPrinterState getCurrentState() {
    return currentState;
  }

  public void setCurrentState(MPrinterState currentState) {
    this.currentState = currentState;
  }

  public MPrinterState getLastTonerSwapCyan() {
    return lastTonerSwapCyan;
  }

  public void setLastTonerSwapCyan(MPrinterState lastTonerSwapCyan) {
    this.lastTonerSwapCyan = lastTonerSwapCyan;
  }

  public MPrinterState getLastTonerSwapMagenta() {
    return lastTonerSwapMagenta;
  }

  public void setLastTonerSwapMagenta(MPrinterState lastTonerSwapMagenta) {
    this.lastTonerSwapMagenta = lastTonerSwapMagenta;
  }

  public MPrinterState getLastTonerSwapYellow() {
    return lastTonerSwapYellow;
  }

  public void setLastTonerSwapYellow(MPrinterState lastTonerSwapYellow) {
    this.lastTonerSwapYellow = lastTonerSwapYellow;
  }

  public MPrinterState getLastTonerSwapBlack() {
    return lastTonerSwapBlack;
  }

  public void setLastTonerSwapBlack(MPrinterState lastTonerSwapBlack) {
    this.lastTonerSwapBlack = lastTonerSwapBlack;
  }

  public MSomeOther getDataWarehouseId() {
    return dataWarehouseId;
  }

  public void setDataWarehouseId(MSomeOther dataWarehouseId) {
    this.dataWarehouseId = dataWarehouseId;
  }
}

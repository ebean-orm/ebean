package org.tests.model.interfaces;

public interface IPerson {
  long getOid();

  IAddress getDefaultAddress();

  void setDefaultAddress(IAddress address);
}

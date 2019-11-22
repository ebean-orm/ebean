package org.tests.model.interfaces;

import java.util.List;

public interface IPerson {
  long getOid();

  IAddress getDefaultAddress();

  void setDefaultAddress(IAddress address);

  List<IAddress> getExtraAddresses();

  List<IAddress> getAddressLinks();
}

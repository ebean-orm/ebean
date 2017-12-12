package io.ebeaninternal.server.cluster;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a List of BinaryMessage's.
 */
public class BinaryMessageList {

  final List<BinaryMessage> list = new ArrayList<>();

  public void add(BinaryMessage msg) {
    list.add(msg);
  }

  public List<BinaryMessage> getList() {
    return list;
  }

}

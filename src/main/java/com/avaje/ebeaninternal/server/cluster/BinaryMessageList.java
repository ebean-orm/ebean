package com.avaje.ebeaninternal.server.cluster;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a List of BinaryMessage's.
 *
 * @author rbygrave
 */
public class BinaryMessageList {

  final ArrayList<BinaryMessage> list = new ArrayList<>();

  public void add(BinaryMessage msg) {
    list.add(msg);
  }

  public List<BinaryMessage> getList() {
    return list;
  }

}

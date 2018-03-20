package io.ebeaninternal.server.deploy;

import java.util.ArrayList;
import java.util.List;

class ElementHelpList implements ElementHelp {

  @Override
  public ElementCollector createCollector() {
    return new Collector();
  }

  private static class Collector implements ElementCollector {

    private List<Object> list = new ArrayList<>();

    @Override
    public void addElement(Object element) {
      list.add(element);
    }

    @Override
    public Object collection() {
      return list;
    }
  }
}

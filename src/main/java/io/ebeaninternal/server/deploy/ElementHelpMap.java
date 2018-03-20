package io.ebeaninternal.server.deploy;

import java.util.LinkedHashMap;
import java.util.Map;

class ElementHelpMap implements ElementHelp {

  @Override
  public ElementCollector createCollector() {
    return new Collector();
  }

  private static class Collector implements ElementCollector {

    private Map<Object, Object> map = new LinkedHashMap<>();

    @Override
    public void addElement(Object element) {
      throw new RuntimeException("asd");
      //map.put()
    }

    @Override
    public Object collection() {
      return map;
    }
  }
}

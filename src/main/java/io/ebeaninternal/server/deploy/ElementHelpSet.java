package io.ebeaninternal.server.deploy;

import java.util.LinkedHashSet;
import java.util.Set;

class ElementHelpSet implements ElementHelp {

  @Override
  public ElementCollector createCollector() {
    return new Collector();
  }

  private static class Collector implements ElementCollector {

    private Set<Object> set = new LinkedHashSet<>();

    @Override
    public void addElement(Object element) {
      set.add(element);
    }

    @Override
    public void addKeyValue(Object key, Object element) {
      throw new IllegalStateException("never called");
    }

    @Override
    public Object collection() {
      return set;
    }
  }
}

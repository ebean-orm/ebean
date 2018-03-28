package io.ebeaninternal.server.deploy;

import io.ebean.bean.BeanCollection;
import io.ebean.common.BeanMap;

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
      throw new IllegalStateException("never called");
    }

    @Override
    public void addKeyValue(Object key, Object element) {
      map.put(key, element);
    }

    @Override
    public Object collection() {
      BeanMap<Object, Object> beanMap = new BeanMap<>(map);
      beanMap.setModifyListening(BeanCollection.ModifyListenMode.ALL);
      return beanMap;
    }
  }
}

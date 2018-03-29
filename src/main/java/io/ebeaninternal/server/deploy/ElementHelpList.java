package io.ebeaninternal.server.deploy;

import io.ebean.bean.BeanCollection;
import io.ebean.common.BeanList;

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
    public void addKeyValue(Object key, Object element) {
      throw new IllegalStateException("never called");
    }

    @Override
    public Object collection() {
      BeanList<Object> beanList = new BeanList<>(list);
      beanList.setModifyListening(BeanCollection.ModifyListenMode.ALL);
      return beanList;
    }
  }
}

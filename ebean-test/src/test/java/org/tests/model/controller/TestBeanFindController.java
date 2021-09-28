package org.tests.model.controller;

import io.ebean.bean.BeanCollection;
import io.ebean.event.BeanFindController;
import io.ebean.event.BeanQueryRequest;
import io.ebean.plugin.BeanType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestBeanFindController implements BeanFindController {

  @Override
  public boolean isRegisterFor(final Class<?> cls) {
    return cls.isAssignableFrom(FindControllerMain.class);
  }

  @Override
  public boolean isInterceptFind(final BeanQueryRequest<?> request) {
    return false;
  }

  @Override
  public <T> T find(final BeanQueryRequest<T> request) {
    return null;
  }

  @Override
  public boolean isInterceptFindMany(final BeanQueryRequest<?> request) {
    return false;
  }

  @Override
  public <T> BeanCollection<T> findMany(final BeanQueryRequest<T> request) {
    return null;
  }

  @Override
  public <T> BeanCollection<T> postProcessMany(final BeanQueryRequest<T> request, final BeanCollection<T> result) {
    Map<Class<?>, List<Integer>> elementsMap = new HashMap<>();
    Map<Class<?>, Map<Integer, FindControllerMain>> controllerLookup = new HashMap<>();

    for (Object entry : result.getActualEntries()) {
      FindControllerMain findControllerMain = (FindControllerMain) entry;
      Class<?> beanType = beanTypeFor(findControllerMain.getTargetTableName(), request);

      if (beanType != null) {
        elementsMap.computeIfAbsent(beanType, key -> new ArrayList<>()).add(findControllerMain.getTargetId());
        controllerLookup.computeIfAbsent(beanType, key -> new HashMap<>()).put(findControllerMain.getTargetId(), findControllerMain);
      }
    }

    elementsMap.forEach((beanType, ids) -> {
      final Map<Integer, FindControllerMain> idLookup = controllerLookup.get(beanType);
      request.database().find(beanType).where()
        .idIn(ids).setMapKey("id")
        .findMap().forEach((id, bean) -> idLookup.get((Integer) id).setTarget(bean));
    });

    return result;
  }

  @Override
  public <T> T postProcess(final BeanQueryRequest<T> request, final T result) {
    FindControllerMain findControllerMain = (FindControllerMain) result;

    Class<?> beanType = beanTypeFor(findControllerMain.getTargetTableName(), request);

    if (beanType != null) {
      findControllerMain.setTarget(request.database().find(beanType, findControllerMain.getTargetId()));
    }

    return result;
  }

  private Class<?> beanTypeFor(String tableName, BeanQueryRequest<?> request) {
    List<? extends BeanType<?>> types = request.database().pluginApi().beanTypes(tableName);
    for (BeanType<?> type : types) {
      if (type.isInheritanceRoot()) {
        return type.type();
      }
    }
    return null;
  }

}

package io.ebeaninternal.server.persist;

import io.ebean.SqlUpdate;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.IntersectionTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Node for processing merge on ManyToMany properties.
 */
class MergeNodeAssocManyToMany extends MergeNode {

  private final BeanPropertyAssocMany<?> many;

  MergeNodeAssocManyToMany(String fullPath, BeanPropertyAssocMany<?> property) {
    super(fullPath, property);
    this.many = property;
  }

  public void merge(MergeRequest request) {

    EntityBean parentBean = request.getBean();

    Collection beans = many.getRawCollection(parentBean);
    Collection outlines = many.getRawCollection(request.getOutline());

    Map<Object, EntityBean> outlineIds = toMap(outlines);

    List<EntityBean> additions = new ArrayList<>();
    if (beans != null) {
      for (Object bean : beans) {
        EntityBean entityBean = (EntityBean) bean;
        Object beanId = targetDescriptor.getId(entityBean);
        if (beanId != null) {
          if (outlineIds.remove(beanId) == null) {
            additions.add(entityBean);
          }
        }
      }
    }

    // any remaining are considered deletes
    List<EntityBean> deletions = new ArrayList<>(outlineIds.values());

    SpiEbeanServer server = request.getServer();
    SpiTransaction transaction = request.getTransaction();

    IntersectionTable intersectionTable = many.intersectionTable();

    if (!deletions.isEmpty()) {
      transaction.flushBatch();

      SqlUpdate delete = intersectionTable.delete(server, false);
      for (EntityBean deletion : deletions) {
        many.intersectionBind(delete, parentBean, deletion);
        delete.addBatch();
      }
      delete.execute();
    }

    if (!additions.isEmpty()) {
      transaction.flushBatch();

      SqlUpdate insert = intersectionTable.insert(server, false);
      for (EntityBean addition : additions) {
        many.intersectionBind(insert, parentBean, addition);
        insert.addBatch();
      }
      insert.execute();
    }

    many.resetMany(parentBean);
  }

}

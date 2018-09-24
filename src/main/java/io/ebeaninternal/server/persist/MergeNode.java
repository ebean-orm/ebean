package io.ebeaninternal.server.persist;

import io.ebean.Query;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssoc;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base class for merge nodes.
 */
abstract class MergeNode {

  protected final String fullPath;
  protected final BeanDescriptor<?> targetDescriptor;
  protected Map<String,MergeNode> children;

  MergeNode(String fullPath, BeanPropertyAssoc<?> property) {
    this.fullPath = fullPath;
    this.targetDescriptor = property.getTargetDescriptor();
  }

  /**
   * Perform the merge processing.
   */
  abstract void merge(MergeRequest request);

  /**
   * Add a child node given the fullPath and relative path.
   */
  MergeNode addChild(String fullPath, String path) {
    MergeNode childNode = MergeHandler.createMergeNode(fullPath, targetDescriptor, path);
    if (children == null) {
      children = new LinkedHashMap<>();
    }
    children.put(path, childNode);
    return childNode;
  }

  /**
   * Return the node given the relative path.
   */
  MergeNode get(String path) {
    if (children != null) {
      return children.get(path);
    }
    return null;
  }

  /**
   * Return the outline beans as a map keyed by Id values.
   */
  Map<Object, EntityBean> toMap(Collection outlines) {

    Map<Object, EntityBean> outlineMap = new HashMap<>();
    if (outlines != null) {
      for (Object out : outlines) {
        EntityBean outlineBean = (EntityBean) out;
        Object outlineId = targetDescriptor.getId(outlineBean);
        outlineMap.put(outlineId, outlineBean);
      }
    }
    return outlineMap;
  }

  /**
   * Add to the query to fetch the Ids values for the foreign keys basically.
   */
  void addSelectId(Query<?> query) {

    BeanProperty idProperty = targetDescriptor.getIdProperty();
    query.fetch(fullPath, idProperty.getName());
  }

  /**
   * Cascade the merge processing if this has child nodes.
   */
  protected void cascade(EntityBean entityBean, EntityBean outlineBean, MergeRequest request) {

    if (children != null && !children.isEmpty()) {
      MergeRequest sub = request.sub(entityBean, outlineBean);
      for (MergeNode node : children.values()) {
        node.merge(sub);
      }
    }
  }
}

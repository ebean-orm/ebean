package io.ebeaninternal.server.persist;

import io.ebean.CacheMode;
import io.ebean.MergeOptions;
import io.ebean.PersistenceContextScope;
import io.ebean.Query;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssoc;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;

import javax.persistence.PersistenceException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Drives the merge processing.
 */
class MergeHandler {

  private static final Pattern PATH_SPLIT = Pattern.compile("\\.");

  private final SpiEbeanServer server;
  private final BeanDescriptor<?> desc;
  private final EntityBean bean;
  private final MergeOptions options;
  private final SpiTransaction transaction;

  private final Map<String, MergeNode> nodes = new LinkedHashMap<>();


  MergeHandler(SpiEbeanServer server, BeanDescriptor<?> desc, EntityBean bean, MergeOptions options, SpiTransaction transaction) {
    this.server = server;
    this.desc = desc;
    this.bean = bean;
    this.options = options;
    this.transaction = transaction;
  }

  /**
   * Fetch the Ids for the graph and use them to determine inserts, updates and deletes for the merge paths.
   */
  List<EntityBean> merge() {

    Set<String> paths = options.paths();
    if (desc.isIdGeneratedValue() && paths.isEmpty() && !options.isClientGeneratedIds()) {
      // just do a single insert or update based on Id value present
      Object id = desc.getId(bean);
      if (id != null) {
        bean._ebean_getIntercept().setForceUpdate(true);
      }
      return Collections.emptyList();
    }

    EntityBean outline = fetchOutline(paths);
    if (outline == null) {
      // considered an insert ...
      return Collections.emptyList();
    }

    // the top level bean is an update
    bean._ebean_getIntercept().setForceUpdate(true);

    // detect what beans are updates and recursively set forceUpdate as needed
    // and outline beans not in the merge graph are generally considered as deletes
    MergeContext context = new MergeContext(server, transaction, options.isClientGeneratedIds());
    MergeRequest request = new MergeRequest(context, bean, outline);
    for (MergeNode value : nodes.values()) {
      value.merge(request);
    }

    return context.getDeletedBeans();
  }

  /**
   * Fetch the outline bean with associated one and associated many beans loaded with Id values only.
   * <p>
   * We use the Id values to determine what are inserts, updates and deletes as part of the merge.
   */
  private EntityBean fetchOutline(Set<String> paths) {

    Query<?> query = server.find(desc.getBeanType());

    query.setBeanCacheMode(CacheMode.OFF);
    query.setPersistenceContextScope(PersistenceContextScope.QUERY);
    query.setId(desc.getId(bean));
    query.select(desc.getIdProperty().getName());

    for (String path : paths) {
      MergeNode node = buildNode(path);
      node.addSelectId(query);
    }
    return (EntityBean) server.findOne(query, transaction);
  }

  private MergeNode buildNode(String path) {
    String[] split = PATH_SPLIT.split(path);
    if (split.length == 1) {
      return addRootLevelNode(split[0]);
    } else {
      return addSubNode(path, split);
    }
  }

  private MergeNode addSubNode(String fullPath, String[] split) {
    MergeNode parent = nodes.get(split[0]);
    if (parent == null) {
      throw new PersistenceException("Unable to find parent path " + split[0] + " in merge paths?");
    }

    for (int i = 1; i < split.length - 1; i++) {
      parent = parent.get(split[i]);
      if (parent == null) {
        throw new PersistenceException("Unable to find parent path " + split[0] + " in merge paths?");
      }
    }
    return parent.addChild(fullPath, split[split.length - 1]);
  }

  private MergeNode addRootLevelNode(String rootPath) {

    MergeNode node = createMergeNode(rootPath, desc, rootPath);
    nodes.put(rootPath, node);
    return node;
  }

  static MergeNode createMergeNode(String fullPath, BeanDescriptor<?> targetDesc, String path) {

    BeanProperty prop = targetDesc.getBeanProperty(path);
    if (prop == null || !(prop instanceof BeanPropertyAssoc)) {
      throw new PersistenceException("merge path [" + path + "] is not a ToMany or ToOne property of " + targetDesc.getFullName());
    }
    if (prop instanceof BeanPropertyAssocMany<?>) {
      BeanPropertyAssocMany<?> assocMany = (BeanPropertyAssocMany<?>) prop;
      if (assocMany.isManyToMany()) {
        return new MergeNodeAssocManyToMany(fullPath, assocMany);
      } else {
        return new MergeNodeAssocOneToMany(fullPath, assocMany);
      }
    } else {
      return new MergeNodeAssocOne(fullPath, (BeanPropertyAssocOne<?>) prop);
    }
  }
}

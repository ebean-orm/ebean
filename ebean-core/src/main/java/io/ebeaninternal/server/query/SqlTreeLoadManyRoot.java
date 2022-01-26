package io.ebeaninternal.server.query;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.DbReadContext;

import java.sql.SQLException;

final class SqlTreeLoadManyRoot extends SqlTreeLoadBean {

  private final STreePropertyAssocMany manyProp;

  SqlTreeLoadManyRoot(SqlTreeNodeManyRoot node) {
    super(node);
    this.manyProp = node.manyProp;
  }

  @Override
  public EntityBean load(DbReadContext cquery, EntityBean parentBean, EntityBean contextParent) throws SQLException {
    // pass in null for parentBean because added to a collection rather than set to the parentBean
    Load load = createLoad(cquery, null);
    EntityBean detailBean = load.perform();
    if (contextParent != null) {
      // Add to the collection and initialise collection if needed
      // A null detailBean may initialise an empty collection
      // Check for bean existing in collection based on load.isContextBean()
      manyProp.addBeanToCollectionWithCreate(contextParent, detailBean, load.isContextBean());
    }
    return detailBean;
  }
}

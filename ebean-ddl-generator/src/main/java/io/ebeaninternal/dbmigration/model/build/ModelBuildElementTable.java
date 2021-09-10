package io.ebeaninternal.dbmigration.model.build;

import io.ebeaninternal.dbmigration.model.MTable;
import io.ebeaninternal.server.deploy.visitor.VisitProperties;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.BeanTable;

/**
 * Add the element collection table to the model.
 */
public class ModelBuildElementTable {

  /**
   * Build and add the MTable model for the ElementCollection property.
   */
  public static void build(ModelBuildContext ctx, BeanPropertyAssocMany<?> manyProp) {

    BeanTable beanTable = manyProp.beanTable();
    BeanDescriptor<?> targetDescriptor = manyProp.targetDescriptor();
    MTable table = new MTable(beanTable.getBaseTable());

    VisitProperties.visit(targetDescriptor, new ModelBuildPropertyVisitor(ctx, table, targetDescriptor));
    ctx.addTableElementCollection(table);
  }

}

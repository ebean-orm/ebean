package io.ebeaninternal.dbmigration.model.build;

import io.ebeaninternal.dbmigration.model.MTable;
import io.ebeaninternal.dbmigration.model.visitor.VisitAllUsing;
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

    BeanTable beanTable = manyProp.getBeanTable();

    BeanDescriptor<?> targetDescriptor = manyProp.getTargetDescriptor();

    MTable table = new MTable(beanTable.getBaseTable());

    VisitAllUsing.visitOne(targetDescriptor, new ModelBuildPropertyVisitor(ctx, table, targetDescriptor));

    ctx.fkeyBuilder(table)
      .addForeignKey(manyProp.getBeanDescriptor(), manyProp.getTableJoin(), true);

    ctx.addTable(table);
  }


}

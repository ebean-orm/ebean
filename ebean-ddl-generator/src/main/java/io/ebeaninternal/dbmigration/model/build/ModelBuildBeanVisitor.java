package io.ebeaninternal.dbmigration.model.build;

import io.ebeaninternal.dbmigration.model.MTable;
import io.ebeaninternal.dbmigration.model.visitor.BeanVisitor;
import io.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Used to build the Model objects MTable etc.
 */
public class ModelBuildBeanVisitor implements BeanVisitor {

  private final ModelBuildContext ctx;

  public ModelBuildBeanVisitor(ModelBuildContext ctx) {
    this.ctx = ctx;
  }

  /**
   * Return the PropertyVisitor used to read all the property meta data
   * and in this case add MColumn objects to the model.
   * <p>
   * This creates an MTable and adds it to the model.
   * </p>
   */
  @Override
  public ModelBuildPropertyVisitor visitBean(BeanDescriptor<?> descriptor) {
    MTable table = new MTable(descriptor);
    // add the table to the model
    ctx.addTable(table);
    return new ModelBuildPropertyVisitor(ctx, table, descriptor);
  }

}

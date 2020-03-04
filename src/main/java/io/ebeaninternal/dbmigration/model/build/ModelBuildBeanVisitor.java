package io.ebeaninternal.dbmigration.model.build;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebeaninternal.dbmigration.model.MColumn;
import io.ebeaninternal.dbmigration.model.MTable;
import io.ebeaninternal.dbmigration.model.visitor.BeanVisitor;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.InheritInfo;

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

    if (!descriptor.isInheritanceRoot()) {
      return null;
    }

    MTable table = new MTable(descriptor);
    // add the table to the model
    ctx.addTable(table);

    InheritInfo inheritInfo = descriptor.getInheritInfo();
    if (inheritInfo != null && inheritInfo.isRoot()) {
      // add the discriminator column
      String discColumn = inheritInfo.getDiscriminatorColumn();
      String columnDefn = inheritInfo.getColumnDefn();
      if (columnDefn == null || columnDefn.isEmpty()) {
        DbPlatformType dbType = ctx.getDbTypeMap().get(inheritInfo.getDiscriminatorType());
        columnDefn = dbType.renderType(inheritInfo.getColumnLength(), 0);
      }
      table.addColumn(new MColumn(discColumn, columnDefn, true));
    }

    return new ModelBuildPropertyVisitor(ctx, table, descriptor);
  }

}

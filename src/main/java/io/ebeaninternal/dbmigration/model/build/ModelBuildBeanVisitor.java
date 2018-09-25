package io.ebeaninternal.dbmigration.model.build;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.IdType;
import io.ebeaninternal.dbmigration.migration.IdentityType;
import io.ebeaninternal.dbmigration.model.MColumn;
import io.ebeaninternal.dbmigration.model.MTable;
import io.ebeaninternal.dbmigration.model.visitor.BeanVisitor;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
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

    MTable table = new MTable(descriptor.getBaseTable());
    table.setPartitionMeta(descriptor.getPartitionMeta());
    table.setComment(descriptor.getDbComment());
    if (descriptor.isHistorySupport()) {
      table.setWithHistory(true);
      BeanProperty whenCreated = descriptor.getWhenCreatedProperty();
      if (whenCreated != null) {
        table.setWhenCreatedColumn(whenCreated.getDbColumn());
      }
    }
    setIdentity(descriptor, table);

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

  /**
   * Set the identity type to use for this table.
   * <p>
   * Takes into account the requested identity type and the underlying support in the
   * database platform.
   * </p>
   */
  private void setIdentity(BeanDescriptor<?> descriptor, MTable table) {

    if (IdType.GENERATOR == descriptor.getIdType()) {
      // explicit generator like UUID
      table.setIdentityType(IdentityType.GENERATOR);
      return;
    }
    if (IdType.EXTERNAL == descriptor.getIdType()) {
      // externally defined code (lookup table, ISO country code etc)
      table.setIdentityType(IdentityType.EXTERNAL);
      return;
    }

    int initialValue = descriptor.getSequenceInitialValue();
    int allocationSize = descriptor.getSequenceAllocationSize();

    if (!descriptor.isIdTypePlatformDefault() || initialValue > 0 || allocationSize > 0) {
      // explicitly set to use sequence or identity (generally not recommended practice)
      if (IdType.IDENTITY == descriptor.getIdType()) {
        if (!descriptor.isIdTypePlatformDefault()) {
          table.setIdentityType(IdentityType.IDENTITY);
        }
      } else {
        // explicit sequence defined
        table.setIdentityType(IdentityType.SEQUENCE);
        table.setSequenceName(descriptor.getSequenceName());
        table.setSequenceInitial(initialValue);
        table.setSequenceAllocate(allocationSize);
      }
    }
  }

}

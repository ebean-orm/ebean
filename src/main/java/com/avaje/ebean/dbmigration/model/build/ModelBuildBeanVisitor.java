package com.avaje.ebean.dbmigration.model.build;

import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.CompoundUniqueContraint;
import com.avaje.ebeaninternal.server.deploy.InheritInfo;
import com.avaje.ebean.dbmigration.model.MColumn;
import com.avaje.ebean.dbmigration.model.MTable;
import com.avaje.ebean.dbmigration.model.visitor.BeanPropertyVisitor;
import com.avaje.ebean.dbmigration.model.visitor.BeanVisitor;

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
   *   This creates an MTable and adds it to the model.
   * </p>
   */
  public BeanPropertyVisitor visitBean(BeanDescriptor<?> descriptor) {

    if (!descriptor.isInheritanceRoot()) {
      return null;
    }

    MTable table = new MTable(descriptor.getBaseTable());

    // add the table to the model
    ctx.addTable(table);

    InheritInfo inheritInfo = descriptor.getInheritInfo();
    if (inheritInfo != null && inheritInfo.isRoot()) {
      // add the discriminator column
      String discColumn = inheritInfo.getDiscriminatorColumn();
      DbType dbType = ctx.getDbTypeMap().get(inheritInfo.getDiscriminatorType());
      String discDbType = dbType.renderType(inheritInfo.getDiscriminatorLength(), 0);

      table.addColumn(new MColumn(discColumn, discDbType, true));
    }

    CompoundUniqueContraint[] compoundUniqueConstraints = descriptor.getCompoundUniqueConstraints();
    if (compoundUniqueConstraints != null) {
      for (int i = 0; i < compoundUniqueConstraints.length; i++) {
        table.addCompoundUniqueConstraint(compoundUniqueConstraints[i].getColumns());
      }
    }

    return new ModelBuildPropertyVisitor(ctx, table);
  }

}

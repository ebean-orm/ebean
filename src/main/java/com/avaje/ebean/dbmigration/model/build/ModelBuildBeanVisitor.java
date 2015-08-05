package com.avaje.ebean.dbmigration.model.build;

import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebean.dbmigration.migration.IdentityType;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.CompoundUniqueContraint;
import com.avaje.ebeaninternal.server.deploy.InheritInfo;
import com.avaje.ebean.dbmigration.model.MColumn;
import com.avaje.ebean.dbmigration.model.MTable;
import com.avaje.ebean.dbmigration.model.visitor.BeanPropertyVisitor;
import com.avaje.ebean.dbmigration.model.visitor.BeanVisitor;
import com.avaje.ebeaninternal.server.type.ScalarType;

import java.sql.Types;

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

    setIdentity(descriptor, table);

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
        table.setIdentityType(IdentityType.IDENTITY);
      } else {
        // explicit sequence defined
        table.setIdentityType(IdentityType.SEQUENCE);
        table.setSequenceName(descriptor.getSequenceName());
        table.setSequenceInitial(initialValue);
        table.setSequenceAllocate(allocationSize);
      }
      return;
    }

    BeanProperty idProperty = descriptor.getIdProperty();
    if (idProperty != null) {
      ScalarType<Object> scalarType = idProperty.getScalarType();
      if (scalarType != null) {
        int jdbcType = scalarType.getJdbcType();
        if (jdbcType == Types.VARCHAR) {
          System.out.println("asd");
        }
      }
    }

  }

}

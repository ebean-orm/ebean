package com.avaje.ebeaninternal.server.deploy.parse;

import com.avaje.ebean.config.TableName;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanTable;

/**
 * Read the annotations for BeanTable.
 * <p>
 * Refer to BeanTable but basically determining base table, table alias
 * and the unique id properties.
 * </p>
 */
public class AnnotationBeanTable extends AnnotationBase {

	final DeployBeanTable beanTable;

    public AnnotationBeanTable(DeployUtil util, DeployBeanTable beanTable){
    	super(util);
        this.beanTable = beanTable;
    }

    /**
     * Parse the annotations.
     */
    public void parse() {
    	
    	TableName tableName = namingConvention.getTableName(beanTable.getBeanType());

		beanTable.setBaseTable(tableName.getQualifiedName());
    }
}

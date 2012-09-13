package com.avaje.ebeaninternal.server.ddl;

import java.util.logging.Logger;

import com.avaje.ebean.config.dbplatform.DbDdlSyntax;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to generate the drop table DDL script.
 */
public class DropSequenceVisitor implements BeanVisitor {

	private static final Logger logger = Logger.getLogger(DropSequenceVisitor.class.getName());
	
	private final DdlGenContext ctx;
	
	private final DbDdlSyntax ddlSyntax;

	private final boolean supportsSequence;
	
	public DropSequenceVisitor(DdlGenContext ctx) {
		this.ctx = ctx;
		this.ddlSyntax = ctx.getDdlSyntax();
		this.supportsSequence = ctx.getDbPlatform().getDbIdentity().isSupportsSequence();
	}
	
	public boolean visitBean(BeanDescriptor<?> descriptor) {
		
		
		if (!descriptor.isInheritanceRoot()){
			return false;
		}
		if (descriptor.getSequenceName() != null) {
			
			if (!supportsSequence){
				// Hopefully a generic test case
				String msg = "Not dropping sequence "+descriptor.getSequenceName()+" on Bean "+descriptor.getName()
					+" as DatabasePlatform does not support sequences";
				logger.finer(msg);
				return false;
			} 
			
			ctx.write("drop sequence ");
			if (ddlSyntax.getDropIfExists() != null){
				ctx.write(ddlSyntax.getDropIfExists()).write(" ");
			}
			ctx.write(descriptor.getSequenceName());			
			ctx.write(";").writeNewLine().writeNewLine();
		}
		return true;
	}

	
	public void visitBeanEnd(BeanDescriptor<?> descriptor) {
	}

	public void visitBegin() {	
	}

	public void visitEnd() {	
	}

	public PropertyVisitor visitProperty(BeanProperty p) {
		// Return null as we are not interested in properties
		return null;
	}
	
}

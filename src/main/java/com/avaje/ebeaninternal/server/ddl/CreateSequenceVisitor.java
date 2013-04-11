package com.avaje.ebeaninternal.server.ddl;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to generate the drop table DDL script.
 */
public class CreateSequenceVisitor implements BeanVisitor {

	private static final Logger logger = LoggerFactory.getLogger(CreateSequenceVisitor.class);
	
	private final DdlGenContext ctx;
	
	private final boolean supportsSequence;

	public CreateSequenceVisitor(DdlGenContext ctx) {
		this.ctx = ctx;
		this.supportsSequence = ctx.getDbPlatform().getDbIdentity().isSupportsSequence();
	}
	
	public boolean visitBean(BeanDescriptor<?> descriptor) {
		
		if (!descriptor.isInheritanceRoot()){
			return false;
		}
		if (descriptor.getSequenceName() == null) {
			return false;
		}
		
		if (!supportsSequence){
			// Hopefully a generic test case
			String msg = "Not creating sequence "+descriptor.getSequenceName()+" on Bean "+descriptor.getName()
				+" as DatabasePlatform does not support sequences";
			logger.warn(msg);
			return false;
		} 

		if (descriptor.getSequenceName() != null) {
			ctx.write("create sequence ");
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

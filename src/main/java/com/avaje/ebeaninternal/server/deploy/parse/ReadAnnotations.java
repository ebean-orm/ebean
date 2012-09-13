/**
 * Copyright (C) 2006  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.ebeaninternal.server.deploy.parse;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;


/**
 * Read the deployment annotations for the bean.
 */
public class ReadAnnotations {

	/**
	 * Read the initial non-relationship annotations included Id and EmbeddedId.
	 * <p>
	 * We then have enough to create BeanTables which are used in readAssociations
	 * to resolve the relationships etc.
	 * </p>
	 */
    public void readInitial(DeployBeanInfo<?> info){

    	try { 		
    		new AnnotationClass(info).parse();
	        new AnnotationFields(info).parse();
	       
    	} catch (RuntimeException e){
    		String msg = "Error reading annotations for "+info;
    		throw new RuntimeException(msg, e);
    	}
    }
    

    /**
     * Read and process the associated relationship annotations.
     * <p>
     * These can only be processed after the BeanTables have been created
     * </p>
     * <p>
     * This uses the factory as a call back to get the BeanTable for a given 
     * associated bean.
     * </p>
     */
    public void readAssociations(DeployBeanInfo<?> info, BeanDescriptorManager factory){
        
    	try {
    		
	        new AnnotationAssocOnes(info, factory).parse();
	        new AnnotationAssocManys(info, factory).parse();
	        	        
	        // read the Sql annotations last because they may be
	        // dependent on field level annotations
	        new AnnotationSql(info).parse();
	        
    	} catch (RuntimeException e){
    		String msg = "Error reading annotations for "+info;
    		throw new RuntimeException(msg, e);
    	}
    }
    
}

package com.avaje.ebeaninternal.server.deploy.parse;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptorManager;


/**
 * Read the deployment annotations for the bean.
 */
public class ReadAnnotations {

  /**
   * Typically _with_history and when appended to the base table derives the name of
   * the view that unions the base table with the history table to support asOf queries.
   */
  private final String asOfViewSuffix;

  public ReadAnnotations(String asOfViewSuffix) {
    this.asOfViewSuffix = asOfViewSuffix;
  }

  /**
	 * Read the initial non-relationship annotations included Id and EmbeddedId.
	 * <p>
	 * We then have enough to create BeanTables which are used in readAssociations
	 * to resolve the relationships etc.
	 * </p>
	 */
    public void readInitial(DeployBeanInfo<?> info, boolean eagerFetchLobs){

    	try { 		
    		new AnnotationClass(info, asOfViewSuffix).parse();
	      new AnnotationFields(info, eagerFetchLobs).parse();
	       
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

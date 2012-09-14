package com.avaje.ebeaninternal.server.subclass;

/**
 * The suffix used build a generated EntityBean class. 
 * <p>
 * Note that the server name can be appended after 
 * </p>
 */
public interface GenSuffix {
    
    /**
     * The suffix added to the super class name.
     */
    public static final String SUFFIX = "$$EntityBean";
    
}

package com.avaje.ebeaninternal.server.persist;

/**
 * Contants used in persist.
 */
public interface Constant {

    /**
     * An INSERT clause.
     */
    static final int IN_INSERT = 1;
    
    /**
     * An UPDATE SET clause.
     */
    static final int IN_UPDATE_SET = 2;
    
    /**
     * An UPDATE WHERE clause.
     */
    static final int IN_UPDATE_WHERE = 3;
    
    /**
     * A DELETE WHERE clause.
     */
    static final int IN_DELETE_WHERE = 4;
}

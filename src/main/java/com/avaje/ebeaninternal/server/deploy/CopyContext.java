package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.server.transaction.DefaultPersistenceContext;

/**
 * Provides context when performing a bean copy.
 * 
 * @author rbygrave
 */
public class CopyContext {

    private final boolean vanillaMode;

    private final boolean sharing;
    
    private final PersistenceContext pc;
    
    public CopyContext(boolean vanillaMode, boolean sharing) {
        this.vanillaMode = vanillaMode;
        this.sharing = sharing;
        this.pc = new DefaultPersistenceContext();
    }

    public CopyContext(boolean vanillaMode) {
        this(vanillaMode, false);
    }

    /**
     * Return true if the copy should be a vanilla bean.
     */
    public boolean isVanillaMode() {
        return vanillaMode;
    }

    /**
     * Return true if the copy should be safe for sharing.
     */
    public boolean isSharing() {
        return sharing;
    }

    /**
     * Return the persistence context used during the copy.
     */
    public PersistenceContext getPersistenceContext() {
        return pc;
    }
    
    /**
     * Put the bean if absent into the persistence context.
     */
    public Object putIfAbsent(Object id, Object bean){
        return pc.putIfAbsent(id, bean);
    }
    
    /**
     * Return the bean for the given type and id from the persistence context.
     */
    public Object get(Class<?> beanType, Object beanId){
        return pc.get(beanType, beanId);
    }
    
}

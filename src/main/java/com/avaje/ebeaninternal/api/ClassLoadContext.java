package com.avaje.ebeaninternal.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the caller and context class loaders.
 * <p>
 * Helper for ClassUtil.
 * </p>
 * 
 * @author rbygrave
 * 
 */
class ClassLoadContext {

    private static final Logger logger = LoggerFactory.getLogger(ClassLoadContext.class);
    
    private final ClassLoader callerLoader;

    private final ClassLoader contextLoader;

    private final boolean preferContext;
    
    private boolean ambiguous;
    
    public static ClassLoadContext of(Class<?> caller, boolean preferContext) {
        return new ClassLoadContext(caller, preferContext);
    }

    /**
     * This constructor is package-private to restrict instantiation to
     * {@link ClassLoadContextFactory} only.
     */
    ClassLoadContext(final Class<?> caller, boolean preferContext) {
        if (caller == null){
            throw new IllegalArgumentException("caller is null");
        }
        this.callerLoader = caller.getClassLoader();
        this.contextLoader = Thread.currentThread().getContextClassLoader();
        this.preferContext = preferContext;
    }

    public Class<?> forName(String name) throws ClassNotFoundException {

        ClassLoader defaultLoader = getDefault(preferContext);

        try {
            return Class.forName(name, true, defaultLoader);
        } catch (ClassNotFoundException e) {
            if (callerLoader == defaultLoader) {
                throw e;
            } else {
                return Class.forName(name, true, callerLoader);
            }
        }
    }

    
    /**
     * Return the expected class loader to use.
     * <p>
     * Works on the assumption that the child of the caller or context class
     * loader is preferred.
     * </p>
     */
    public ClassLoader getDefault(boolean preferContext) {
        
        if (contextLoader == null){
            if (logger.isDebugEnabled()){
                logger.debug("No Context ClassLoader, using "+callerLoader.getClass().getName());
            }
            return callerLoader;
        }
        if (contextLoader == callerLoader){
          if (logger.isDebugEnabled()){
                logger.debug("Context and Caller ClassLoader's same instance of "+contextLoader.getClass().getName());
            }
            return callerLoader;
        }
        
        if (isChild(contextLoader, callerLoader)) {
          if (logger.isDebugEnabled()){
                logger.debug("Caller ClassLoader "+callerLoader.getClass().getName()
                        +" child of ContextLoader "+contextLoader.getClass().getName());
            }
            return callerLoader;
            
        } else if (isChild(callerLoader, contextLoader)) {
          if (logger.isDebugEnabled()){
                logger.debug("Context ClassLoader "+contextLoader.getClass().getName()
                        +" child of Caller ClassLoader "+callerLoader.getClass().getName());
            }
            return contextLoader;
            
        } else {            
            // ambiguous case, perhaps both null
            logger.debug("Ambiguous ClassLoader choice preferContext:"+preferContext
                    +" Context:"+contextLoader.getClass().getName()+" Caller:"+callerLoader.getClass().getName());
            ambiguous = true;
            return preferContext ? contextLoader : callerLoader;
        }
    }

    /**
     * Return true if the 'default' class loader is ambiguous.
     */
    public boolean isAmbiguous() {
        return ambiguous;
    }

    /**
     * Return the ClassLoader of the caller.
     */
    public ClassLoader getCallerLoader() {
        return callerLoader;
    }

    /**
     * Return the Thread Context ClassLoader.
     */
    public ClassLoader getContextLoader() {
        return contextLoader;
    }

    /**
     * Return the ClassLoader for this class.
     */
    public ClassLoader getThisLoader() {
        return this.getClass().getClassLoader();
    }

    /**
     * Returns 'true' if 'loader2' is a delegation child of 'loader1' [or if
     * 'loader1'=='loader2'].
     */
    private boolean isChild(final ClassLoader loader1, ClassLoader loader2) {
//        if (loader1 == loader2) {
//            logger.info("Context and Caller ClassLoader's same "+loader1.getClass().getName());
//            return true;
//        }
//        if (loader2 == null) {
//            logger.info(msg+" ClassLoader is null");
//            return false;
//        }
//        if (loader1 == null) {
//            logger.info("Using "+msg+" ClassLoader as other is null");
//            return true;
//        }

        for (; loader2 != null; loader2 = loader2.getParent()) {
            if (loader2 == loader1) {
                return true;
            }
        }

        return false;
    }

}
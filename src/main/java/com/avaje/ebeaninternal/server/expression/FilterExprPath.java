package com.avaje.ebeaninternal.server.expression;

import java.io.Serializable;

/**
 * This is the path prefix for filterMany.
 * <p>
 * The actual path can change due to FetchConfig query joins that proceed
 * the query that includes the filterMany.
 * </p>
 * 
 * @author rbygrave
 */
public class FilterExprPath implements Serializable {

    private static final long serialVersionUID = -6420905565372842018L;
    
    /**
     * The path of the filterMany.
     */
    private String path;
    
    public FilterExprPath(String path){
        this.path = path;
    }
    
    /**
     * Trim off leading part of the path due to a 
     * proceeding (earlier) query join etc.
     */
    public void trimPath(int prefixTrim) {
        path = path.substring(prefixTrim);
    }

    /**
     * Return the path. This is a prefix used in the filterMany expressions.
     */
    public String getPath() {
        return path;
    }

}

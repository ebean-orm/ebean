package com.avaje.ebeaninternal.api;

import com.avaje.ebean.ExpressionFactory;
import com.avaje.ebeaninternal.server.expression.FilterExprPath;

public interface SpiExpressionFactory extends ExpressionFactory {

    /**
     * Create another expression factory with a given sub path.
     */
    public ExpressionFactory createExpressionFactory();//FilterExprPath prefix);
    
}

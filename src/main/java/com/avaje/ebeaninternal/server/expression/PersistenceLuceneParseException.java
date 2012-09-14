package com.avaje.ebeaninternal.server.expression;

import javax.persistence.PersistenceException;

/**
 * Exception used to wrap Lucene parsing exceptions.
 */
public class PersistenceLuceneParseException extends PersistenceException {

    private static final long serialVersionUID = 838790249273928392L;

    public PersistenceLuceneParseException(Throwable e){
        super(e);
    }
    
}

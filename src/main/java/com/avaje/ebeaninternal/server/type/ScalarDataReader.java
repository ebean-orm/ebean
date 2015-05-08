package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;

/**
 * Reads from and binds to database columns.
 */
public interface ScalarDataReader<T> {

    /**
     * Read and return the appropriate value from the dataReader.
     */
    public T read(DataReader dataReader) throws SQLException;

    /**
     * Ignore typically by moving the index position.
     */
    public void loadIgnore(DataReader dataReader);

    /**
     * Bind the value to the underlying preparedStatement.
     */
    public void bind(DataBind b, T value) throws SQLException;

    /**
     * Accumulate all the scalar types used by an immutable compound value type.
     */
    public void accumulateScalarTypes(String propName, CtCompoundTypeScalarList list);

}

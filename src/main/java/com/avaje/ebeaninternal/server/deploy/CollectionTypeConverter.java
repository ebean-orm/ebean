package com.avaje.ebeaninternal.server.deploy;

/**
 * Used to convert between collection types.
 * <P>
 * This typically means wrap and unwrap mutable scala collection types of Buffer, Set and Map.
 * </p>
 * 
 * @author rbygrave
 *
 */
public interface CollectionTypeConverter {

    /**
     * Convert the wrapped type to the underlying Java List, Set or Map.
     */
    public Object toUnderlying(Object wrapped);

    /**
     * Wrap the underlying Java List, Set or Map into the final collection type.
     */
    public Object toWrapped(Object wrapped);

}

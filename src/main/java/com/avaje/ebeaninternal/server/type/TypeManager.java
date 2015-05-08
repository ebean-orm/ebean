package com.avaje.ebeaninternal.server.type;

import com.avaje.ebeaninternal.server.type.reflect.CheckImmutableResponse;

/**
 * Convert an Object to the required type.
 */
public interface TypeManager {

    /**
     * Check if the type is immutable using reflection.
     */
    public CheckImmutableResponse checkImmutable(Class<?> cls);

    /**
     * Create ScalarDataReader's for the Immutable compound type. 
     */
    public ScalarDataReader<?> recursiveCreateScalarDataReader(Class<?> cls);

    /**
     * Create ScalarTypes for this Immutable Value Object type.
     */
    public ScalarType<?> recursiveCreateScalarTypes(Class<?> cls);

	/**
	 * Register a ScalarType with the system.
	 */
	public void add(ScalarType<?> scalarType);

	/**
	 * Return the Internal CompoundType handler for a given compound type.
	 */
    public CtCompoundType<?> getCompoundType(Class<?> type);

	/**
	 * Return the ScalarType for a given jdbc type.
	 * 
	 * @param jdbcType
	 *            as per java.sql.Types
	 */
	public ScalarType<?> getScalarType(int jdbcType);

	/**
	 * Return the ScalarType for a given logical type.
	 */
	public <T> ScalarType<T> getScalarType(Class<T> type);

	/**
	 * For java.util.Date and java.util.Calendar additionally pass the jdbc type
	 * that you would like the ScalarType to map to. This is because these types
	 * can map to different java.sql.Types depending on the property.
	 */
	public <T> ScalarType<T> getScalarType(Class<T> type, int jdbcType);

	/**
	 * Create a ScalarType for an Enum using a mapping (rather than JPA Ordinal
	 * or String which has limitations).
	 */
	public ScalarType<?> createEnumScalarType(Class<?> enumType);

	/**
	 * Find a scalarType using a custom type key. Used for Hstore and similar special types.
	 */
  public ScalarType<?> getScalarTypeFromKey(String specialTypeKey);
}

package io.ebean;

import org.jspecify.annotations.Nullable;

/**
 * Options to be used with insert such as ON CONFLICT DO UPDATE | NOTHING.
 */
public interface InsertOptions {

  /**
   * Use ON CONFLICT UPDATE with automatic determination of the unique columns to conflict on.
   * <p>
   * Uses mapping to determine the unique columns - {@code @Column(unique=true)} and {@code @Index(unique=true)} .
   */
  InsertOptions ON_CONFLICT_UPDATE = InsertOptions.builder()
    .onConflictUpdate()
    .build();

  /**
   * Use ON CONFLICT DO NOTHING with automatic determination of the unique columns to conflict on.
   * <p>
   * Uses mapping to determine the unique columns - {@code @Column(unique=true)} and {@code @Index(unique=true)} .
   */
  InsertOptions ON_CONFLICT_NOTHING = InsertOptions.builder()
    .onConflictNothing()
    .build();

  /**
   * Return a builder for InsertOptions.
   */
  static Builder builder() {
    return new DInsertOptionsBuilder();
  }

  /**
   * Return the constraint name that is used for ON CONFLICT.
   */
  @Nullable
  String constraint();

  /**
   * Return the unique columns that is used for ON CONFLICT.
   * <p>
   * When not explicitly set will use mapping like {@code @Column(unique=true)} to determine the
   * non-unique columns.
   */
  @Nullable
  String uniqueColumns();

  /**
   * Return the ON CONFLICT UPDATE SET clause.
   * <p>
   * When not set will use the non-unique columns.
   */
  @Nullable
  String updateSet();

  /**
   * Return if GetGeneratedKeys should be used to fetch the generated keys after insert.
   */
  @Nullable
  Boolean getGetGeneratedKeys();

  /**
   * Return the key for these build options.
   */
  String key();

  /**
   * The builder for InsertOptions.
   */
  interface Builder {

    /**
     * Use a ON CONFLICT UPDATE automatically determining the unique columns.
     */
    Builder onConflictUpdate();

    /**
     * Use a ON CONFLICT DO NOTHING automatically determining the unique columns.
     */
    Builder onConflictNothing();

    /**
     * Specify an explicit conflict constraint name.
     * <p>
     * When this is used then unique columns will not be used.
     */
    Builder constraint(String constraint);

    /**
     * Specify the unique columns for the conflict target.
     * <p>
     * When not specified and constraint is also not specified then
     * it will automatically determine the unique columns
     * based on mapping like {@code @Column(unique=true)} and
     * {@code @Index(unique=true)} .
     */
    Builder uniqueColumns(String uniqueColumns);

    /**
     * Specify the ON CONFLICT DO UPDATE SET clause.
     * <p>
     * When not specified ebean will include all the non-unique columns.
     */
    Builder updateSet(String updateSet);

    /**
     * Specify if GetGeneratedKeys should be used to return generated keys.
     */
    Builder getGeneratedKeys(boolean getGeneratedKeys);

    /**
     * Build and return the insert options.
     */
    InsertOptions build();

  }
}

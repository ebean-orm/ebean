package com.avaje.ebeaninternal.server.deploy;

/**
 * Holds multiple column unique constraints defined for an entity.
 */
public class CompoundUniqueContraint {

    private final String[] columns;

    public CompoundUniqueContraint(String[] columns) {
        this.columns = columns;
    }

    /**
     * Return the columns that make up this unique constraint.
     */
    public String[] getColumns() {
        return columns;
    }
    
}

package com.avaje.ebeaninternal.server.deploy;

import java.util.Set;

/**
 * Converts logical property names to database columns for the raw sql.
 * <p>
 * This is used in building the where and having clauses for SqlSelect queries.
 * </p>
 */
public final class DeployPropertyParserRawSql extends DeployParser {

    private final DRawSqlSelect rawSqlSelect;

    public DeployPropertyParserRawSql(DRawSqlSelect rawSqlSelect) {
        this.rawSqlSelect = rawSqlSelect;
    }

    /**
     * Returns null for raw sql queries.
     */
    public Set<String> getIncludes() {
        return null;
    }

    public String convertWord() {
        String r = getDeployWord(word);
        return r == null ? word : r;
    }

    @Override
    public String getDeployWord(String expression) {
        DRawSqlColumnInfo columnInfo = rawSqlSelect.getRawSqlColumnInfo(expression);
        if (columnInfo == null) {
            return null;
        } else {
            return columnInfo.getName();
        }
    }

}

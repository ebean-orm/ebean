package io.ebean.config.dbplatform.sqlserver;

import io.ebean.config.dbplatform.DbStandardHistorySupport;

/**
 * History support only valid on SqlServer 2016 or later.
 *
 * @author Vilmos Nagy  <vilmos.nagy@outlook.com>
 */
public class SqlServerHistorySupport extends DbStandardHistorySupport {

    /**
     * Return the ' as of timestamp ?' clause appended after the table name.
     */
    @Override
    public String getAsOfViewSuffix(String asOfViewSuffix) {
        return " for system_time as of ?";
    }

    @Override
    public String getVersionsBetweenSuffix(String asOfViewSuffix) {
        return " for system_time between ? and ?";
    }

    /**
     * Returns the SQL Server specific effective start column.
     */
    @Override
    public String getSysPeriodLower(String tableAlias, String sysPeriod) {
        return tableAlias + "." + sysPeriod + "From";
    }

    /**
     * Returns the SQL Server specific effective end column.
     */
    @Override
    public String getSysPeriodUpper(String tableAlias, String sysPeriod) {
        return tableAlias + "." + sysPeriod + "To";
    }
}

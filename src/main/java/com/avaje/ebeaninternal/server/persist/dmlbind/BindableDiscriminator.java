package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.List;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.InheritInfo;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for inserting a discriminator value.
 */
public class BindableDiscriminator implements Bindable {

    private final String columnName;
    private final Object discValue;
    private final int sqlType;

    public BindableDiscriminator(InheritInfo inheritInfo) {
        this.columnName = inheritInfo.getDiscriminatorColumn();
        this.discValue = inheritInfo.getDiscriminatorValue();
        this.sqlType = inheritInfo.getDiscriminatorType();
    }

    public String toString() {
        return columnName + " = " + discValue;
    }

    public void addChanged(PersistRequestBean<?> request, List<Bindable> list) {
        throw new PersistenceException("Never called (only for inserts)");
    }

    public void dmlInsert(GenerateDmlRequest request, boolean checkIncludes) {
        dmlAppend(request, checkIncludes);
    }

    /**
     * Never used in where clause.
     */
    public void dmlWhere(GenerateDmlRequest request, boolean checkIncludes, Object bean) {
        // never used in where
    }

    public void dmlAppend(GenerateDmlRequest request, boolean checkIncludes) {
        request.appendColumn(columnName);
    }

    public void dmlBind(BindableRequest bindRequest, boolean checkIncludes, Object bean) throws SQLException {

        bindRequest.bind(columnName, discValue, sqlType);
    }

    public void dmlBindWhere(BindableRequest bindRequest, boolean checkIncludes, Object bean) throws SQLException {

        bindRequest.bind(columnName, discValue, sqlType);
    }

}

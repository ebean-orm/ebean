package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.List;

import com.avaje.ebeaninternal.api.DerivedRelationshipData;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.id.ImportedId;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for an ManyToOne or OneToOne associated bean.
 */
public class BindableAssocOne implements Bindable {

    private final BeanPropertyAssocOne<?> assocOne;

    private final ImportedId importedId;

    public BindableAssocOne(BeanPropertyAssocOne<?> assocOne) {
        this.assocOne = assocOne;
        this.importedId = assocOne.getImportedId();
    }

    public String toString() {
        return "BindableAssocOne " + assocOne;
    }

    public void addChanged(PersistRequestBean<?> request, List<Bindable> list) {
        if (request.hasChanged(assocOne)) {
            list.add(this);
        }
    }

    public void dmlInsert(GenerateDmlRequest request, boolean checkIncludes) {
        dmlAppend(request, checkIncludes);
    }

    public void dmlAppend(GenerateDmlRequest request, boolean checkIncludes) {
        if (checkIncludes && !request.isIncluded(assocOne)) {
            return;
        }
        importedId.dmlAppend(request);
    }

    /**
     * Used for dynamic where clause generation.
     */
    public void dmlWhere(GenerateDmlRequest request, boolean checkIncludes, Object bean) {
        if (checkIncludes && !request.isIncludedWhere(assocOne)) {
            return;
        }
        Object assocBean = assocOne.getValue(bean);
        importedId.dmlWhere(request, assocBean);
    }

    public void dmlBind(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {
        if (checkIncludes && !request.isIncluded(assocOne)) {
            return;
        }
        dmlBind(request, bean, true);
    }

    public void dmlBindWhere(BindableRequest request, boolean checkIncludes, Object bean) throws SQLException {
        if (checkIncludes && !request.isIncludedWhere(assocOne)) {
            return;
        }
        dmlBind(request, bean, false);
    }

    private void dmlBind(BindableRequest request, Object bean, boolean bindNull)
            throws SQLException {

        Object assocBean = assocOne.getValue(bean);
        Object boundValue = importedId.bind(request, assocBean, bindNull);
        if (bindNull && boundValue == null && assocBean != null){
        	// this is the scenario for a derived foreign key
        	// which will require an additional update 
        	// register for post insert of assocBean
        	// update of bean set ... importedId.getLogicalName();
        	// value of assocBean.getId
        	DerivedRelationshipData d = new DerivedRelationshipData(assocBean, assocOne.getName(), bean);
        	request.registerDerivedRelationship(d);
        }
    }

}

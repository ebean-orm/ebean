package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyCompound;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Bindable for a Immutable Compound value object.
 */
public class BindableCompound implements Bindable {

    private final Bindable[] items;

    private final BeanPropertyCompound compound;

    public BindableCompound(BeanPropertyCompound embProp, List<Bindable> list) {
        this.compound = embProp;
        this.items = list.toArray(new Bindable[list.size()]);
    }

    public String toString() {
        return "BindableCompound " + compound + " items:" + Arrays.toString(items);
    }

    public void dmlInsert(GenerateDmlRequest request, boolean checkIncludes) {
        dmlAppend(request, checkIncludes);
    }

    public void dmlAppend(GenerateDmlRequest request, boolean checkIncludes) {
        if (checkIncludes && !request.isIncluded(compound)) {
            return;
        }

        for (int i = 0; i < items.length; i++) {
            items[i].dmlAppend(request, false);
        }
    }

    /**
     * Used for dynamic where clause generation.
     */
    public void dmlWhere(GenerateDmlRequest request, boolean checkIncludes, Object origBean) {
        if (checkIncludes && !request.isIncludedWhere(compound)) {
            return;
        }

        Object valueObject = compound.getValue(origBean);

        for (int i = 0; i < items.length; i++) {
            items[i].dmlWhere(request, false, valueObject);
        }
    }

    public void addChanged(PersistRequestBean<?> request, List<Bindable> list) {
        if (request.hasChanged(compound)) {
            list.add(this);
        }
    }

    public void dmlBind(BindableRequest bindRequest, boolean checkIncludes, Object bean) throws SQLException {
        if (checkIncludes && !bindRequest.isIncluded(compound)) {
            return;
        }

        Object valueObject = compound.getValue(bean);

        for (int i = 0; i < items.length; i++) {
            items[i].dmlBind(bindRequest, false, valueObject);
        }
    }

    public void dmlBindWhere(BindableRequest bindRequest, boolean checkIncludes, Object bean) throws SQLException {
        if (checkIncludes && !bindRequest.isIncludedWhere(compound)) {
            return;
        }

        Object valueObject = compound.getValue(bean);

        for (int i = 0; i < items.length; i++) {
            items[i].dmlBindWhere(bindRequest, false, valueObject);
        }
    }
}

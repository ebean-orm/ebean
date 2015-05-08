package com.avaje.ebeaninternal.server.persist.dmlbind;

import java.sql.SQLException;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;

/**
 * Item held by Meta objects used to generate and bind bean insert update and
 * delete statements.
 * <p>
 * An implementation is expected to be immutable and thread safe.
 * </p>
 * <p>
 * The design is to take a bean structure with embedded and associated objects
 * etc and flatten that into lists of Bindable objects. These are put into
 * InsertMeta UpdateMeta and DeleteMeta objects to support the generation of DML
 * and binding of statements in a fast and painless manor.
 * </p>
 */
public interface Bindable {

    /**
     * For Updates including only changed properties add the Bindable to the
     * list if it should be included in the 'update set'.
     */
    public void addToUpdate(PersistRequestBean<?> request, List<Bindable> list);

    /**
     * append sql to the buffer with prefix and suffix options.
     */
    public void dmlAppend(GenerateDmlRequest request);

    /**
     * Bind given the request and bean. The bean could be the oldValues bean
     * when binding a update or delete where clause with ALL concurrency mode.
     */
    public void dmlBind(BindableRequest request, EntityBean bean) throws SQLException;

}

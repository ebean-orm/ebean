package io.ebeaninternal.server.persist.dml;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DbEncrypt;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.persist.dmlbind.Bindable;
import io.ebeaninternal.server.persist.dmlbind.BindableId;
import io.ebeaninternal.server.persist.dmlbind.BindableList;
import io.ebeaninternal.server.persist.dmlbind.BindableOrderColumn;
import io.ebeaninternal.server.persist.dmlbind.BindableUnidirectional;
import io.ebeaninternal.server.persist.dmlbind.FactoryAssocOnes;
import io.ebeaninternal.server.persist.dmlbind.FactoryBaseProperties;
import io.ebeaninternal.server.persist.dmlbind.FactoryEmbedded;
import io.ebeaninternal.server.persist.dmlbind.FactoryId;
import io.ebeaninternal.server.persist.dmlbind.FactoryVersion;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating InsertMeta UpdateMeta and DeleteMeta.
 */
public class MetaFactory {

  private final FactoryBaseProperties baseFact;
  private final FactoryEmbedded embeddedFact;
  private final FactoryVersion versionFact = new FactoryVersion();
  private final FactoryAssocOnes assocOneFact = new FactoryAssocOnes();

  private final FactoryId idFact = new FactoryId();

  /**
   * Include Lobs in the base statement. Generally true. Oracle9 used to require
   * a separate statement for Clobs and Blobs.
   */
  private static final boolean includeLobs = true;

  private final DatabasePlatform dbPlatform;

  private final boolean emptyStringAsNull;

  MetaFactory(DatabasePlatform dbPlatform) {
    this.dbPlatform = dbPlatform;
    this.emptyStringAsNull = dbPlatform.isTreatEmptyStringsAsNull();

    // to bind encryption data before or after the encryption key
    DbEncrypt dbEncrypt = dbPlatform.getDbEncrypt();
    boolean bindEncryptDataFirst = dbEncrypt == null || dbEncrypt.isBindEncryptDataFirst();

    this.baseFact = new FactoryBaseProperties(bindEncryptDataFirst);
    this.embeddedFact = new FactoryEmbedded(bindEncryptDataFirst);
  }

  /**
   * Create the UpdateMeta for the given bean type.
   */
  UpdateMeta createUpdate(BeanDescriptor<?> desc) {

    List<Bindable> setList = new ArrayList<>();

    baseFact.create(setList, desc, DmlMode.UPDATE, includeLobs);
    embeddedFact.create(setList, desc, DmlMode.UPDATE, includeLobs);
    assocOneFact.create(setList, desc, DmlMode.UPDATE);

    BeanProperty orderColumn = desc.getOrderColumn();
    if (orderColumn != null) {
      setList.add(new BindableOrderColumn(orderColumn));
    }

    BindableId id = idFact.createId(desc);
    Bindable version = versionFact.create(desc);
    Bindable tenantId = versionFact.createTenantId(desc);

    BindableList setBindable = new BindableList(setList);

    return new UpdateMeta(emptyStringAsNull, desc, setBindable, id, version, tenantId);
  }

  /**
   * Create the DeleteMeta for the given bean type.
   */
  DeleteMeta createDelete(BeanDescriptor<?> desc) {

    BindableId id = idFact.createId(desc);
    Bindable version = versionFact.createForDelete(desc);
    Bindable tenantId = versionFact.createTenantId(desc);

    return new DeleteMeta(emptyStringAsNull, desc, id, version, tenantId);
  }

  /**
   * Create the InsertMeta for the given bean type.
   */
  InsertMeta createInsert(BeanDescriptor<?> desc) {

    BindableId id = idFact.createId(desc);

    List<Bindable> allList = new ArrayList<>();

    baseFact.create(allList, desc, DmlMode.INSERT, includeLobs);
    embeddedFact.create(allList, desc, DmlMode.INSERT, includeLobs);
    assocOneFact.create(allList, desc, DmlMode.INSERT);

    BindableList allBindable = new BindableList(allList);

    BeanPropertyAssocOne<?> unidirectional = desc.getUnidirectional();

    Bindable shadowFkey;
    if (unidirectional == null) {
      shadowFkey = null;
    } else {
      shadowFkey = new BindableUnidirectional(desc, unidirectional);
    }

    return new InsertMeta(dbPlatform, desc, shadowFkey, id, allBindable);
  }
}

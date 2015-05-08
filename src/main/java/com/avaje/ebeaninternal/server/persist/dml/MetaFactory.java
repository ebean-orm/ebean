package com.avaje.ebeaninternal.server.persist.dml;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.DbEncrypt;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.persist.dmlbind.Bindable;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableId;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableList;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableUnidirectional;
import com.avaje.ebeaninternal.server.persist.dmlbind.FactoryAssocOnes;
import com.avaje.ebeaninternal.server.persist.dmlbind.FactoryBaseProperties;
import com.avaje.ebeaninternal.server.persist.dmlbind.FactoryEmbedded;
import com.avaje.ebeaninternal.server.persist.dmlbind.FactoryId;
import com.avaje.ebeaninternal.server.persist.dmlbind.FactoryVersion;

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

  public MetaFactory(DatabasePlatform dbPlatform) {
    this.dbPlatform = dbPlatform;
    this.emptyStringAsNull = dbPlatform.isTreatEmptyStringsAsNull();

    // to bind encryption data before or after the encryption key
    DbEncrypt dbEncrypt = dbPlatform.getDbEncrypt();
    boolean bindEncryptDataFirst = dbEncrypt == null ? true : dbEncrypt.isBindEncryptDataFirst();

    this.baseFact = new FactoryBaseProperties(bindEncryptDataFirst);
    this.embeddedFact = new FactoryEmbedded(bindEncryptDataFirst);
  }

  /**
   * Create the UpdateMeta for the given bean type.
   */
  public UpdateMeta createUpdate(BeanDescriptor<?> desc) {

    List<Bindable> setList = new ArrayList<Bindable>();

    baseFact.create(setList, desc, DmlMode.UPDATE, includeLobs);
    embeddedFact.create(setList, desc, DmlMode.UPDATE, includeLobs);
    assocOneFact.create(setList, desc, DmlMode.UPDATE);

    BindableId id = idFact.createId(desc);

    Bindable ver = versionFact.create(desc);

    BindableList setBindable = new BindableList(setList);

    return new UpdateMeta(emptyStringAsNull, desc, setBindable, id, ver);
  }

  /**
   * Create the DeleteMeta for the given bean type.
   */
  public DeleteMeta createDelete(BeanDescriptor<?> desc) {

    BindableId id = idFact.createId(desc);

    Bindable ver = versionFact.create(desc);

    return new DeleteMeta(emptyStringAsNull, desc, id, ver);
  }

  /**
   * Create the InsertMeta for the given bean type.
   */
  public InsertMeta createInsert(BeanDescriptor<?> desc) {

    BindableId id = idFact.createId(desc);

    List<Bindable> allList = new ArrayList<Bindable>();

    baseFact.create(allList, desc, DmlMode.INSERT, includeLobs);
    embeddedFact.create(allList, desc, DmlMode.INSERT, includeLobs);
    assocOneFact.create(allList, desc, DmlMode.INSERT);

    Bindable allBindable = new BindableList(allList);

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

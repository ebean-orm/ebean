package io.ebeaninternal.server.persist.dmlbind;

import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;

final class MatchedImportedFactory {

  /**
   * Create the array of matchedImportedProperty based on the properties and descriptor.
   */
  static MatchedImportedProperty[] build(BeanProperty[] props, BeanDescriptor<?> desc) {
    MatchedImportedProperty[] matches = new MatchedImportedProperty[props.length];
    for (int i = 0; i < props.length; i++) {
      // find matching assoc one property for dbColumn
      matches[i] = findMatch(props[i], desc);
      if (matches[i] == null) {
        // ok, the assoc ones are not on the bean?
        return null;
      }
    }
    return matches;
  }

  private static MatchedImportedProperty findMatch(BeanProperty prop, BeanDescriptor<?> desc) {
    // find matching against the local database column
    String dbColumn = prop.getDbColumn();
    for (BeanPropertyAssocOne<?> assocOne1 : desc.propertiesOne()) {
      if (assocOne1.isImportedPrimaryKey()) {
        // search using the ImportedId from the assoc one
        BeanProperty foreignMatch = assocOne1.getImportedId().findMatchImport(dbColumn);
        if (foreignMatch != null) {
          return new MatchedImportedEmbedded(prop, assocOne1, foreignMatch);
        }
      }
    }
    for (BeanProperty beanProperty : desc.propertiesBaseScalar()) {
      if (dbColumn.equals(beanProperty.getDbColumn())) {
        return new MatchedImportedScalar(prop, beanProperty);
      }
    }
    // there was no matching assoc one property.
    // example UserRole bean missing assoc one to User?
    return null;
  }
}

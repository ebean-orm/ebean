package io.ebean.plugin;

import io.ebeaninternal.server.deploy.id.ImportedId;

public interface PropertyAssocOne extends PropertyAssoc {

  boolean isOneToOne();

  ImportedId getImportedId();

}

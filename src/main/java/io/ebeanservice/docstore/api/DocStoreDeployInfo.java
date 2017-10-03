package io.ebeanservice.docstore.api;

import io.ebean.annotation.DocStore;
import io.ebean.annotation.DocStoreMode;
import io.ebean.text.PathProperties;

public interface DocStoreDeployInfo<T> {

  boolean isDocStoreMapped();

  PathProperties getDocStorePathProperties();

  DocStore getDocStore();

  String getDocStoreQueueId();

  String getDocStoreIndexName();

  String getDocStoreIndexType();

  DocStoreMode getDocStoreInsertEvent();

  DocStoreMode getDocStoreUpdateEvent();

  DocStoreMode getDocStoreDeleteEvent();

}

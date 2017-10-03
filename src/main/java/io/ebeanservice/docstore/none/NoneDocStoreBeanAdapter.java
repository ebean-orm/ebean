package io.ebeanservice.docstore.none;

import io.ebean.event.BeanPersistRequest;
import io.ebean.plugin.BeanType;
import io.ebeanservice.docstore.api.DocStoreDeployInfo;
import io.ebeanservice.docstore.api.DocStoreUpdateContext;
import io.ebeanservice.docstore.api.support.DocStoreBeanBaseAdapter;

import java.io.IOException;

/**
 * DocStoreBeanBaseAdapter that barfs if it is used.
 */
public class NoneDocStoreBeanAdapter<T> extends DocStoreBeanBaseAdapter<T> {

  public NoneDocStoreBeanAdapter(BeanType<T> desc, DocStoreDeployInfo<T> deploy) {
    super(desc, deploy);
  }

  @Override
  public boolean isMapped() {
    return false;
  }

  @Override
  public void deleteById(Object idValue, DocStoreUpdateContext txn) throws IOException {
    throw NoneDocStore.implementationNotInClassPath();
  }

  @Override
  public void index(Object idValue, T entityBean, DocStoreUpdateContext txn) throws IOException {
    throw NoneDocStore.implementationNotInClassPath();
  }

  @Override
  public void insert(Object idValue, BeanPersistRequest<T> persistRequest, DocStoreUpdateContext txn) throws IOException {
    throw NoneDocStore.implementationNotInClassPath();
  }

  @Override
  public void update(Object idValue, BeanPersistRequest<T> persistRequest, DocStoreUpdateContext txn) throws IOException {
    throw NoneDocStore.implementationNotInClassPath();
  }

  @Override
  public void updateEmbedded(Object idValue, String embeddedProperty, String embeddedRawContent, DocStoreUpdateContext txn) throws IOException {
    throw NoneDocStore.implementationNotInClassPath();
  }
}

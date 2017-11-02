package io.ebeaninternal.server.deploy.id;

import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.persist.platform.MultiValueBind;

/**
 * Creates the appropriate IdConvertSet depending on the type of Id property(s).
 */
public class IdBinderFactory {

  private static final IdBinderEmpty EMPTY = new IdBinderEmpty();

  private final boolean idInExpandedForm;

  private final MultiValueBind multiValueBind;

  public IdBinderFactory(boolean idInExpandedForm, MultiValueBind multiValueBind) {
    this.idInExpandedForm = idInExpandedForm;
    this.multiValueBind = multiValueBind;
  }

  /**
   * Create the IdConvertSet for the given type of Id properties.
   */
  public IdBinder createIdBinder(BeanProperty id) {

    if (id == null) {
      // for report type beans that don't need an id
      return EMPTY;

    }
    if (id.isEmbedded()) {
      return new IdBinderEmbedded(idInExpandedForm, (BeanPropertyAssocOne<?>) id);
    } else {
      return new IdBinderSimple(id, multiValueBind);
    }
  }

}

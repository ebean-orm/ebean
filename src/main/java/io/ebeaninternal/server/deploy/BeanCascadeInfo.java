package io.ebeaninternal.server.deploy;

import javax.persistence.CascadeType;

/**
 * Persist info for determining if save or delete should be performed.
 * <p>
 * This is set to associated Beans, Table joins and List.
 * </p>
 */
public class BeanCascadeInfo {

  private boolean delete;

  private boolean save;

  private boolean refresh;

  public void setTypes(CascadeType[] types) {
    for (CascadeType type : types) {
      setType(type);
    }
  }

  public void setType(CascadeType type) {
    switch (type) {
      case ALL:
        save = true;
        delete = true;
        refresh = true;
        break;
      case REMOVE:
        delete = true;
        break;
      case REFRESH:
        refresh = true;
        break;
      case PERSIST:
        save = true;
        break;
      case MERGE:
        save = true;
        break;
      default:
        throw new IllegalStateException("Unexpected CascadeType " + type);
    }
  }

  /**
   * Return true if refresh should cascade.
   */
  public boolean isRefresh() {
    return refresh;
  }

  /**
   * Return true if delete should cascade.
   */
  public boolean isDelete() {
    return delete;
  }

  /**
   * Set to true if delete should cascade.
   */
  public void setDelete(boolean delete) {
    this.delete = delete;
  }

  /**
   * Return true if save should cascade.
   */
  public boolean isSave() {
    return save;
  }

  /**
   * Set cascade save and delete settings.
   */
  public void setSaveDelete(boolean save, boolean delete) {
    this.save = save;
    this.delete = delete;
  }

}

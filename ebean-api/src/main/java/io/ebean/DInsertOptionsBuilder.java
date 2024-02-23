package io.ebean;

final class DInsertOptionsBuilder implements InsertOptions.Builder {

  private Boolean getGeneratedKeys;
  private boolean onConflictUpdate;
  private boolean onConflictNothing;
  private String constraint;
  private String uniqueColumns;
  private String updateSet;

  @Override
  public InsertOptions.Builder onConflictNothing() {
    this.onConflictNothing = true;
    return this;
  }

  @Override
  public InsertOptions.Builder onConflictUpdate() {
    this.onConflictUpdate = true;
    return this;
  }

  @Override
  public InsertOptions.Builder constraint(String constraint) {
    this.constraint = constraint;
    return this;
  }

  @Override
  public InsertOptions.Builder uniqueColumns(String uniqueColumns) {
    this.uniqueColumns = uniqueColumns;
    return this;
  }

  @Override
  public InsertOptions.Builder updateSet(String updateSet) {
    this.updateSet = updateSet;
    return this;
  }

  @Override
  public InsertOptions.Builder getGeneratedKeys(boolean getGeneratedKeys) {
    this.getGeneratedKeys = getGeneratedKeys;
    return this;
  }

  @Override
  public InsertOptions build() {
    return new Options(constraint, uniqueColumns, updateSet, onConflictUpdate, onConflictNothing, getGeneratedKeys);
  }

  static final class Options implements InsertOptions {

    private static final String UPDATE = "U";
    private static final String NOTHING = "N";
    private static final String NORMAL = "_";
    private final String key;
    private final Boolean getGeneratedKeys;
    private final String constraint;
    private final String uniqueColumns;
    private final String updateSet;

    Options(String constraint, String uniqueColumns, String updateSet, boolean onConflictUpdate, boolean onConflictNothing, Boolean getGeneratedKeys) {
      this.constraint = constraint;
      this.uniqueColumns = uniqueColumns;
      this.updateSet = updateSet;
      this.getGeneratedKeys = getGeneratedKeys;
      this.key = (onConflictUpdate ? UPDATE : onConflictNothing ? NOTHING : NORMAL)
        + '+' + plus(constraint)
        + '+' + plus(uniqueColumns)
        + '+' + plus(updateSet);
    }

    private String plus(String val) {
      return val == null ? "" : val;
    }

    @Override
    public String key() {
      return key;
    }

    @Override
    public String constraint() {
      return constraint;
    }

    @Override
    public String uniqueColumns() {
      return uniqueColumns;
    }

    @Override
    public String updateSet() {
      return updateSet;
    }

    @Override
    public Boolean getGetGeneratedKeys() {
      return getGeneratedKeys;
    }
  }
}

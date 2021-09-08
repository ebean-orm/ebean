package io.ebeaninternal.server.deploy;

import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.query.STreeProperty;
import io.ebeaninternal.server.query.SqlJoinType;

import java.util.List;

/**
 * Abstract base for dynamic properties.
 */
abstract class DynamicPropertyBase implements STreeProperty {

  private final String name;
  final String fullName;
  private final String elPrefix;
  final ScalarType<?> scalarType;

  DynamicPropertyBase(String name, String fullName, String elPrefix, ScalarType<?> scalarType) {
    this.name = name;
    this.fullName = fullName;
    this.elPrefix = elPrefix;
    this.scalarType = scalarType;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String fullName() {
    return fullName;
  }

  @Override
  public boolean isId() {
    return false;
  }

  @Override
  public boolean isEmbedded() {
    return false;
  }

  @Override
  public boolean isFormula() {
    return false;
  }

  @Override
  public String elPrefix() {
    return elPrefix;
  }

  @Override
  public ScalarType<?> scalarType() {
    return scalarType;
  }

  @Override
  public void buildRawSqlSelectChain(String prefix, List<String> selectChain) {
    // do nothing, only for RawSql
  }

  @Override
  public void loadIgnore(DbReadContext ctx) {
    scalarType.loadIgnore(ctx.getDataReader());
  }

  @Override
  public void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {
    // do not add to from usually
  }

  @Override
  public String encryptKeyAsString() {
    return null;
  }
}

package io.ebeaninternal.dbmigration.model;

import io.ebean.annotation.IdentityGenerated;
import io.ebean.config.dbplatform.IdType;
import io.ebeaninternal.dbmigration.migration.CreateTable;
import io.ebeaninternal.dbmigration.migration.IdentityType;
import io.ebeaninternal.server.deploy.IdentityMode;

import java.math.BigInteger;

/**
 * Helper to convert between IdentityMode and CreateTable
 */
class MTableIdentity {

  /**
   * Return the IdentityMode from CreateTable.
   */
  static IdentityMode fromCreateTable(CreateTable createTable) {

    IdType type = fromType(createTable.getIdentityType());
    int start = toInt(createTable.getSequenceInitial());
    int increment = toInt(createTable.getSequenceAllocate());
    String seqName = createTable.getSequenceName();

    return new IdentityMode(type, IdentityGenerated.AUTO, start, increment, seqName);
  }

  /**
   * Set the IdentityMode to the CreateTable model.
   */
  public static void toCreateTable(IdentityMode identityMode, CreateTable createTable) {

    if (!identityMode.isPlatformDefault()) {
      createTable.setIdentityType(toType(identityMode.getIdType()));
    }
    final String seqName = identityMode.getSequenceName();
    if (seqName != null && !seqName.isEmpty()) {
      createTable.setSequenceName(seqName);
    }

    createTable.setSequenceInitial(toBigInteger(identityMode.getStart()));
    createTable.setSequenceAllocate(toBigInteger(identityMode.getIncrement()));
  }


  private static IdType fromType(IdentityType type) {
    if (type == null) {
      return IdType.AUTO;
    }
    switch (type) {
      case DEFAULT:
        return IdType.AUTO;
      case SEQUENCE:
        return IdType.SEQUENCE;
      case IDENTITY:
        return IdType.IDENTITY;
      case GENERATOR:
        return IdType.GENERATOR;
      case EXTERNAL:
        return IdType.EXTERNAL;
    }
    return IdType.AUTO;
  }

  private static IdentityType toType(IdType type) {
    if (type == null) {
      // intersection or element collection table
      return null;
    }
    switch (type) {
      case SEQUENCE:
        return IdentityType.SEQUENCE;
      case IDENTITY:
        return IdentityType.IDENTITY;
      case EXTERNAL:
        return IdentityType.EXTERNAL;
      case GENERATOR:
        return IdentityType.GENERATOR;
    }
    return null;
  }

  private static int toInt(BigInteger value) {
    return (value == null) ? 0 : value.intValue();
  }

  private static BigInteger toBigInteger(int value) {
    return (value == 0) ? null : BigInteger.valueOf(value);
  }
}

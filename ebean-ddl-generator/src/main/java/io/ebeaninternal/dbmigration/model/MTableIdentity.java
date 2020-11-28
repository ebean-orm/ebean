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
public class MTableIdentity {

  /**
   * Return the IdentityMode from CreateTable.
   */
  public static IdentityMode fromCreateTable(CreateTable createTable) {

    IdType type = fromType(createTable.getIdentityType());
    IdentityGenerated generated = fromGenerated(createTable.getIdentityGenerated());
    int start = toInt(createTable.getIdentityStart(), createTable.getSequenceInitial());
    int increment = toInt(createTable.getIdentityIncrement(), createTable.getSequenceAllocate());
    int cache = toInt(createTable.getIdentityCache(), null);
    String seqName = createTable.getSequenceName();

    return new IdentityMode(type, generated, start, increment, cache, seqName);
  }

  private static IdentityGenerated fromGenerated(String identityGenerated) {
    if (identityGenerated == null) {
      return IdentityGenerated.AUTO;
    }
    return IdentityGenerated.valueOf(identityGenerated.toUpperCase());
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

    createTable.setIdentityStart(toBigInteger(identityMode.getStart()));
    createTable.setIdentityIncrement(toBigInteger(identityMode.getIncrement()));
    createTable.setIdentityCache(toBigInteger(identityMode.getCache()));
    final IdentityGenerated generated = identityMode.getGenerated();
    if (generated != null && generated != IdentityGenerated.AUTO) {
      createTable.setIdentityGenerated(generated.name().toLowerCase());
    }
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

  private static int toInt(BigInteger firstVal, BigInteger secVal) {
    if (firstVal != null) {
      return firstVal.intValue();
    }
    return (secVal == null) ? 0 : secVal.intValue();
  }

  private static BigInteger toBigInteger(int value) {
    return (value == 0) ? null : BigInteger.valueOf(value);
  }
}
